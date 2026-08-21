package com.lennit.cryptolyzer.domain

import com.lennit.cryptolyzer.contracts.Outcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EvmAddressTest {

    companion object {
        // The four canonical EIP-55 reference vectors.
        const val VECTOR_1 = "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed"
        const val VECTOR_2 = "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359"
        const val VECTOR_3 = "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB"
        const val VECTOR_4 = "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb"
    }

    @ParameterizedTest
    @ValueSource(strings = [VECTOR_1, VECTOR_2, VECTOR_3, VECTOR_4])
    fun `derives the EIP-55 checksum from a lowercase address`(expected: String) {
        assertEquals(expected, address(expected.lowercase()).checksummed)
    }

    @ParameterizedTest
    @ValueSource(strings = [VECTOR_1, VECTOR_2, VECTOR_3, VECTOR_4])
    fun `accepts an all-uppercase body, which carries no checksum information`(vector: String) {
        val upper = "0x" + vector.substring(2).uppercase()
        assertEquals(vector, address(upper).checksummed)
    }

    @Test
    fun `rejects a mixed-case address whose checksum fails`() {
        // Vector 1 with a single character case-flipped: exactly the typo class EIP-55 detects.
        val corrupted = "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1Beaed"
        val result = EvmAddress.parse(corrupted)
        assertTrue(result is Outcome.Failure, "a bad checksum must be refused, not lowercased")
        assertTrue((result as Outcome.Failure).error.message.contains("EIP-55"), result.error.message)
    }

    @Test
    fun `rejects malformed input`() {
        mapOf(
            "" to "empty",
            "0x" to "prefix only",
            "5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed" to "missing 0x prefix",
            "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAe" to "39 nibbles",
            "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAedd" to "41 nibbles",
            "0xZZAeb6053F3E94C9b9A09f33669435E7Ef1BeAed" to "non-hex characters",
            "0x0000000000000000000000000000000000000 00" to "embedded space",
        ).forEach { (input, why) ->
            assertTrue(EvmAddress.parse(input) is Outcome.Failure, "should have rejected $why")
        }
    }

    @Test
    fun `identity is case-insensitive so one account is never counted as two`() {
        val lower = address(VECTOR_1.lowercase())
        val checksummed = address(VECTOR_1)
        assertEquals(lower, checksummed)
        assertEquals(lower.hashCode(), checksummed.hashCode())
        assertEquals(1, setOf(lower, checksummed).size)
    }

    @Test
    fun `surrounding whitespace from a paste or a QR scan is tolerated`() {
        assertEquals(VECTOR_1, address("  $VECTOR_1\n").checksummed)
    }

    @Test
    fun `the zero address parses, so it can be recognised and refused by policy rather than crashing`() {
        assertEquals(42, address("0x" + "0".repeat(40)).checksummed.length)
    }
}
