package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.Clock
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import com.lennit.cryptolyzer.telemetry.Telemetry

/** Handles exactly one event type. Must be idempotent: delivery is at-least-once. */
public fun interface EventHandler {
    public suspend fun handle(event: EventEnvelope): Outcome<Unit>
}

public data class DrainReport(
    val claimed: Int,
    val processed: Int,
    val retried: Int,
    val deadLettered: Int,
    val unroutable: Int,
) {
    public val hadWork: Boolean get() = claimed > 0
}

/**
 * Moves events from the durable log to their handlers.
 *
 * The disposition table is exhaustive by construction, which is the point:
 *
 *   handler succeeded        -> Processed
 *   handler failed, retryable, budget left -> Pending with backoff
 *   handler failed, not retryable          -> DeadLettered
 *   handler failed, budget exhausted       -> DeadLettered
 *   handler threw                          -> classified Unknown, dead-lettered (a defect, not a blip)
 *   no handler registered                  -> lease released, stays Pending
 *
 * There is no branch that removes an event without recording a terminal state, so the Phase 3
 * gate holds mechanically rather than by convention.
 */
public class EventDispatcher(
    private val store: EventStore,
    private val clock: Clock,
    private val retryPolicy: RetryPolicy = RetryPolicy(),
    private val telemetry: Telemetry = Telemetry.noop(),
    private val leaseMillis: Long = 30_000,
    private val unroutableBackoffMillis: Long = 60_000,
) {
    private val handlers = LinkedHashMap<String, EventHandler>()

    public fun register(type: EventType, handler: EventHandler): EventDispatcher {
        require(!handlers.containsKey(type.name)) { "Handler already registered for ${type.name}" }
        handlers[type.name] = handler
        return this
    }

    public fun registeredTypes(): Set<String> = handlers.keys.toSet()

    public fun publish(envelope: EventEnvelope): Outcome<AppendResult> {
        val result = store.append(envelope)
        when (result) {
            is Outcome.Success -> when (result.value) {
                is AppendResult.Appended -> telemetry.counter(
                    "event.appended",
                    fields = mapOf("type" to envelope.type.name),
                )
                is AppendResult.Duplicate -> telemetry.counter(
                    "event.duplicate_suppressed",
                    fields = mapOf("type" to envelope.type.name),
                )
            }
            is Outcome.Failure -> telemetry.failure("event.append_failed", result.error)
        }
        return result
    }

    /** Processes one batch. Returns what happened so callers can decide whether to keep draining. */
    public suspend fun drainOnce(batchSize: Int = 32): Outcome<DrainReport> {
        val now = clock.nowEpochMillis()
        val claimed = when (val claim = store.claimPending(batchSize, now, leaseMillis)) {
            is Outcome.Success -> claim.value
            is Outcome.Failure -> return Outcome.failure(claim.error)
        }

        var processed = 0
        var retried = 0
        var deadLettered = 0
        var unroutable = 0

        for (stored in claimed) {
            val handler = handlers[stored.envelope.type.name]
            if (handler == null) {
                store.releaseLease(stored.eventId, now + unroutableBackoffMillis)
                unroutable++
                telemetry.counter("event.unroutable", fields = mapOf("type" to stored.envelope.type.name))
                continue
            }

            val outcome = try {
                handler.handle(stored.envelope)
            } catch (throwable: Throwable) {
                if (throwable is kotlinx.coroutines.CancellationException) throw throwable
                Outcome.failure(
                    PlatformError.Unknown(
                        "Handler for ${stored.envelope.type.name} threw ${throwable::class.simpleName}",
                        cause = throwable,
                    ),
                )
            }

            when (outcome) {
                is Outcome.Success -> {
                    store.markProcessed(stored.eventId, clock.nowEpochMillis())
                    processed++
                    telemetry.counter("event.processed", fields = mapOf("type" to stored.envelope.type.name))
                }
                is Outcome.Failure -> {
                    val attemptsSoFar = stored.attempt + 1
                    val canRetry = outcome.error.retryable && retryPolicy.hasBudgetLeft(attemptsSoFar)
                    if (canRetry) {
                        val delay = retryPolicy.delayFor(attemptsSoFar, stored.eventId)
                        store.markForRetry(
                            stored.eventId,
                            clock.nowEpochMillis() + delay,
                            outcome.error.toString(),
                        )
                        retried++
                        telemetry.counter(
                            "event.retry_scheduled",
                            fields = mapOf(
                                "type" to stored.envelope.type.name,
                                "attempt" to attemptsSoFar.toString(),
                            ),
                        )
                    } else {
                        val reason = if (outcome.error.retryable) {
                            "retry budget exhausted after $attemptsSoFar attempts: ${outcome.error}"
                        } else {
                            "non-retryable: ${outcome.error}"
                        }
                        store.markDeadLettered(stored.eventId, reason, clock.nowEpochMillis())
                        deadLettered++
                        telemetry.failure(
                            "event.dead_lettered",
                            outcome.error,
                            fields = mapOf("type" to stored.envelope.type.name),
                        )
                    }
                }
            }
        }

        return Outcome.success(
            DrainReport(
                claimed = claimed.size,
                processed = processed,
                retried = retried,
                deadLettered = deadLettered,
                unroutable = unroutable,
            ),
        )
    }
}
