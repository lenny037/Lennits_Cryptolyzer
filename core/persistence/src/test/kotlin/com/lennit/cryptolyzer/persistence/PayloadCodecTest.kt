package com.lennit.cryptolyzer.persistence

import com.lennit.cryptolyzer.contracts.Outcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class PayloadCodecTest {

    private fun roundTrip(payload: Map<String, String>) {
        val decoded = PayloadCodec.decode(PayloadCodec.encode(payload))
        assertTrue(decoded is Outcome.Success, "decode failed for $payload: $decoded")
        assertEquals(payload, (decoded as Outcome.Success).value)
    }

    @Test
    fun `round-trips an empty payload`() = roundTrip(emptyMap())

    @Test
    fun `round-trips separator characters that would otherwise corrupt the record`() {
        roundTrip(mapOf("a=b" to "c;d", "back\\slash" to "line1\nline2", "" to ""))
    }

    @Test
    fun `round-trips values that look like the wire format itself`() {
        roundTrip(mapOf("payload" to "x=1;y=2", "escaped" to "\\e\\s\\n"))
    }

    @Test
    fun `round-trips unicode and long values`() {
        roundTrip(mapOf("emoji-free-unicode" to "Ξ ₿ 日本語", "long" to "9".repeat(5_000)))
    }

    @Test
    fun `encoding is deterministic regardless of insertion order`() {
        val first = PayloadCodec.encode(linkedMapOf("z" to "1", "a" to "2", "m" to "3"))
        val second = PayloadCodec.encode(linkedMapOf("m" to "3", "z" to "1", "a" to "2"))
        assertEquals(first, second)
        assertEquals("a=2;m=3;z=1", first)
    }

    @Test
    fun `rejects a malformed record instead of guessing`() {
        assertTrue(PayloadCodec.decode("novalue") is Outcome.Failure)
        assertTrue(PayloadCodec.decode("k=v;trailing\\") is Outcome.Failure)
    }

    companion object {
        @JvmStatic
        fun adversarialValues(): List<String> = listOf(
            "=", ";", "\\", "\\\\", "=;=", "a\\=b", "\n", "\r\n", "  ", "0", "null",
        )
    }

    @ParameterizedTest
    @MethodSource("adversarialValues")
    fun `every adversarial value survives a round trip as both key and value`(value: String) {
        roundTrip(mapOf(value to value, "stable" to "1"))
    }
}
