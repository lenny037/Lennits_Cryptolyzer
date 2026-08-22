package com.lennit.cryptolyzer.persistence

import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError

/**
 * Deterministic encoding for event payloads.
 *
 * Requirements that ruled out the obvious options:
 *  - **Deterministic byte-for-byte**: keys are sorted, so the same payload always produces the
 *    same string and content fingerprints stay stable across app versions. JSON object ordering
 *    is not guaranteed by most encoders.
 *  - **No reflection, no codegen**: this sits on the hot path of a battery-constrained device and
 *    inside a module that must remain platform-free.
 *  - **Lossless**: separators inside keys or values are escaped rather than rejected, so an
 *    arbitrary provider string cannot corrupt the log.
 *
 * Format: `key=value;key=value`, with `\` escaping for `\`, `=`, `;` and newlines.
 */
public object PayloadCodec {

    public fun encode(payload: Map<String, String>): String =
        payload.toSortedMap().entries.joinToString(";") { (key, value) ->
            "${escape(key)}=${escape(value)}"
        }

    public fun decode(encoded: String): Outcome<Map<String, String>> {
        if (encoded.isEmpty()) return Outcome.success(emptyMap())
        val result = LinkedHashMap<String, String>()
        val key = StringBuilder()
        val value = StringBuilder()
        var readingValue = false
        var escaping = false
        var index = 0

        fun flushPair(): PlatformError? {
            if (!readingValue) {
                return PlatformError.Storage("Malformed payload: entry without '=' at index $index")
            }
            result[key.toString()] = value.toString()
            key.clear()
            value.clear()
            readingValue = false
            return null
        }

        while (index < encoded.length) {
            val char = encoded[index]
            val target = if (readingValue) value else key
            when {
                escaping -> {
                    target.append(unescapeChar(char))
                    escaping = false
                }
                char == '\\' -> escaping = true
                char == '=' && !readingValue -> readingValue = true
                char == ';' -> flushPair()?.let { return Outcome.failure(it) }
                else -> target.append(char)
            }
            index++
        }
        if (escaping) {
            return Outcome.failure(PlatformError.Storage("Malformed payload: trailing escape character"))
        }
        flushPair()?.let { return Outcome.failure(it) }
        return Outcome.success(result)
    }

    private fun escape(text: String): String = buildString(text.length) {
        text.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '=' -> append("\\e")
                ';' -> append("\\s")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(char)
            }
        }
    }

    private fun unescapeChar(char: Char): Char = when (char) {
        'e' -> '='
        's' -> ';'
        'n' -> '\n'
        'r' -> '\r'
        else -> char
    }
}
