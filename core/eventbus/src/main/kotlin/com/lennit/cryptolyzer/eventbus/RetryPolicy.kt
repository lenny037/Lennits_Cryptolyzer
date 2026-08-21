package com.lennit.cryptolyzer.eventbus

/**
 * Deterministic exponential backoff with bounded, seeded jitter.
 *
 * Jitter is derived from the event id rather than a random source: retry timing stays spread out
 * across events while remaining exactly reproducible in tests and in incident replay. A random
 * backoff is untestable, and an unjittered backoff synchronises every queued event into the same
 * wake-up, which on a battery-constrained device is a measurable cost.
 */
public data class RetryPolicy(
    val maxAttempts: Int = 5,
    val baseDelayMillis: Long = 1_000,
    val maxDelayMillis: Long = 15 * 60_000,
    val jitterFraction: Double = 0.20,
) {
    init {
        require(maxAttempts >= 1) { "maxAttempts must be at least 1" }
        require(baseDelayMillis > 0) { "baseDelayMillis must be positive" }
        require(maxDelayMillis >= baseDelayMillis) { "maxDelayMillis must be at least baseDelayMillis" }
        require(jitterFraction in 0.0..0.5) { "jitterFraction must be within [0, 0.5]" }
    }

    public fun hasBudgetLeft(attempt: Int): Boolean = attempt < maxAttempts

    /** Delay before the attempt that follows [attempt] completed attempts. */
    public fun delayFor(attempt: Int, eventId: String): Long {
        require(attempt >= 1) { "attempt is 1-based" }
        val exponent = (attempt - 1).coerceAtMost(30)
        val raw = baseDelayMillis.toDouble() * Math.pow(2.0, exponent.toDouble())
        val capped = raw.coerceAtMost(maxDelayMillis.toDouble())
        val seed = (eventId.hashCode().toLong() and 0xffff).toDouble() / 0xffff.toDouble()
        val jitter = capped * jitterFraction * (seed - 0.5) * 2.0
        return (capped + jitter).toLong().coerceIn(baseDelayMillis, maxDelayMillis)
    }
}
