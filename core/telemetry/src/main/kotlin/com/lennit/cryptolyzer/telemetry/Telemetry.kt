package com.lennit.cryptolyzer.telemetry

import com.lennit.cryptolyzer.contracts.PlatformError

/**
 * Structured, redaction-aware telemetry port.
 *
 * There is no string-formatted logging API here on purpose: fields stay structured so that
 * redaction is enforceable and so that on-device log volume stays bounded. Sinks are pluggable,
 * which is how the same core runs under a JVM test, under Logcat, and under an optional
 * remote collector.
 */
public interface Telemetry {

    public fun event(name: String, fields: Map<String, String> = emptyMap())

    public fun counter(name: String, delta: Long = 1, fields: Map<String, String> = emptyMap())

    public fun gauge(name: String, value: Double, fields: Map<String, String> = emptyMap())

    public fun failure(name: String, error: PlatformError, fields: Map<String, String> = emptyMap())

    public companion object {
        public fun noop(): Telemetry = NoopTelemetry
    }
}

private object NoopTelemetry : Telemetry {
    override fun event(name: String, fields: Map<String, String>) = Unit
    override fun counter(name: String, delta: Long, fields: Map<String, String>) = Unit
    override fun gauge(name: String, value: Double, fields: Map<String, String>) = Unit
    override fun failure(name: String, error: PlatformError, fields: Map<String, String>) = Unit
}

/**
 * Field-level redaction applied before any sink sees a value.
 *
 * Addresses, keys, tokens and amounts are the four things that must never land in a shipped log
 * in raw form. Redaction happens centrally rather than at each call site, because a rule that
 * relies on every future developer remembering it is not a control.
 */
public object Redactor {

    private val sensitiveKeyFragments = listOf(
        "key", "secret", "token", "mnemonic", "seed", "password", "signature", "authorization",
    )

    private val addressPattern = Regex("0x[0-9a-fA-F]{40}")

    public fun redactFields(fields: Map<String, String>): Map<String, String> =
        fields.mapValues { (key, value) ->
            if (sensitiveKeyFragments.any { key.lowercase().contains(it) }) "[redacted]" else redactValue(value)
        }

    public fun redactValue(value: String): String =
        addressPattern.replace(value) { match ->
            val text = match.value
            text.substring(0, 6) + "..." + text.substring(text.length - 4)
        }
}

/** Telemetry sink that records in memory. Used by tests and by the on-device diagnostics screen. */
public class RecordingTelemetry : Telemetry {

    public data class Record(val kind: String, val name: String, val fields: Map<String, String>)

    private val recorded = mutableListOf<Record>()

    public val records: List<Record> get() = recorded.toList()

    override fun event(name: String, fields: Map<String, String>) {
        recorded += Record("event", name, Redactor.redactFields(fields))
    }

    override fun counter(name: String, delta: Long, fields: Map<String, String>) {
        recorded += Record("counter", name, Redactor.redactFields(fields) + ("delta" to delta.toString()))
    }

    override fun gauge(name: String, value: Double, fields: Map<String, String>) {
        recorded += Record("gauge", name, Redactor.redactFields(fields) + ("value" to value.toString()))
    }

    override fun failure(name: String, error: PlatformError, fields: Map<String, String>) {
        recorded += Record(
            "failure",
            name,
            Redactor.redactFields(fields) + mapOf(
                "error.code" to error.code,
                "error.severity" to error.severity.name,
                "error.retryable" to error.retryable.toString(),
            ),
        )
    }

    public fun clear() {
        recorded.clear()
    }
}
