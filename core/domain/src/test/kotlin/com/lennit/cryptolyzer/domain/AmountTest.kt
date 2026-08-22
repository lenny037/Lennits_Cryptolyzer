package com.lennit.cryptolyzer.domain

import com.lennit.cryptolyzer.contracts.Outcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

class AmountTest {

    @Test
    fun `decimal arithmetic is exact where binary floating point is not`() {
        assertEquals(amount("0.3"), amount("0.1") + amount("0.2"))
        // The defect class this type exists to eliminate:
        assertTrue(0.1 + 0.2 != 0.3)
    }

    @Test
    fun `equality and hashing ignore trailing-zero scale differences`() {
        assertEquals(amount("1.50"), amount("1.5"))
        assertEquals(amount("1.50").hashCode(), amount("1.5").hashCode())
        assertEquals(amount("0.00"), Amount.ZERO)
    }

    @Test
    fun `converts to chain-native base units`() {
        assertEquals(BigInteger("1500000000000000000"), amount("1.5").toBaseUnits(18).expect())
        assertEquals(BigInteger("1000000"), amount("1").toBaseUnits(6).expect())
        assertEquals(BigInteger.ZERO, Amount.ZERO.toBaseUnits(18).expect())
    }

    @Test
    fun `refuses a conversion that would silently truncate value`() {
        // A 6-decimal asset cannot represent a 7-decimal amount. Rounding here loses money.
        val result = amount("1.0000001").toBaseUnits(6)
        assertTrue(result is Outcome.Failure, "precision loss must be refused, got $result")
        assertTrue((result as Outcome.Failure).error.message.contains("without loss"), result.error.message)
    }

    @Test
    fun `round trips through base units for both large and small magnitudes`() {
        listOf("1234.567890123456789", "0.000000000000000001", "123456789.5").forEach { text ->
            val original = amount(text)
            assertEquals(original, Amount.fromBaseUnits(original.toBaseUnits(18).expect(), 18))
        }
    }

    @Test
    fun `rejects a non-numeric literal instead of coercing it to zero`() {
        listOf("", "  ", "NaN", "Infinity", "1e", "1.2.3", "twelve", "0x10").forEach { text ->
            assertTrue(Amount.parse(text) is Outcome.Failure, "should have rejected '$text'")
        }
    }

    @Test
    fun `division states its scale and rounding mode explicitly`() {
        val third = amount("1").divide(amount("3"), scale = 18, rounding = RoundingMode.DOWN).expect()
        assertEquals(amount("0.333333333333333333"), third)
    }

    @Test
    fun `division by zero is a reported failure, never an exception or an infinity`() {
        assertTrue(amount("1").divide(Amount.ZERO, 18, RoundingMode.DOWN) is Outcome.Failure)
        assertTrue(amount("1").ratioTo(Amount.ZERO) is Outcome.Failure)
    }

    @Test
    fun `ratio expresses a share of a total`() {
        assertEquals(0, amount("25").ratioTo(amount("100")).expect().compareTo(BigDecimal("0.25")))
    }

    @Test
    fun `comparison orders by numeric value, not lexicographically`() {
        val sorted = listOf(amount("10"), amount("9.5"), amount("100"), amount("-1")).sorted()
        assertEquals(listOf(amount("-1"), amount("9.5"), amount("10"), amount("100")), sorted)
    }

    @Test
    fun `sign helpers and negation behave for debit entries`() {
        assertEquals(amount("-5"), amount("5").negate())
        assertEquals(amount("5"), amount("-5").abs())
        assertTrue(amount("-0.01").isNegative)
        assertTrue(amount("0.01").isPositive)
        assertTrue(Amount.ZERO.isZero)
        assertTrue(!Amount.ZERO.isPositive, "zero is not positive: a zero-value trade is not a trade")
    }

    @Test
    fun `display rendering truncates rather than rounds up`() {
        assertEquals("1.99", amount("1.999").toPlainString(scale = 2))
        assertEquals("1.00", amount("1").toPlainString(scale = 2))
    }
}
