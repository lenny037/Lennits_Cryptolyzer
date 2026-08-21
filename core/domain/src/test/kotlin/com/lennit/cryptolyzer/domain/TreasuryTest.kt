package com.lennit.cryptolyzer.domain

import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TreasuryTest {

    private fun holding(total: String, reserved: String, asset: Asset = usdcOnBase) =
        TreasurySnapshot.Holding(asset = asset, total = amount(total), reserved = amount(reserved))

    private fun snapshot(vararg holdings: TreasurySnapshot.Holding) =
        TreasurySnapshot.of(takenAtEpochMillis = 1_000, holdings = holdings.toList())

    private fun failure(result: Outcome<*>): PlatformError {
        assertTrue(result is Outcome.Failure, "expected a refusal, got $result")
        return (result as Outcome.Failure).error
    }

    @Test
    fun `accepts a consistent snapshot and derives available balance`() {
        val treasury = snapshot(holding(total = "100", reserved = "30")).expect()
        assertEquals(amount("100"), treasury.totalOf(usdcOnBase))
        assertEquals(amount("70"), treasury.availableOf(usdcOnBase))
    }

    @Test
    fun `an unheld asset reports zero rather than throwing`() {
        val treasury = snapshot(holding("100", "0")).expect()
        assertEquals(Amount.ZERO, treasury.totalOf(ethOnBase))
        assertEquals(Amount.ZERO, treasury.availableOf(ethOnBase))
    }

    @Test
    fun `refuses a negative total`() {
        val error = failure(snapshot(holding("-1", "0")))
        assertEquals("treasury.non-negative-total", (error as PlatformError.InvariantViolation).invariant)
    }

    @Test
    fun `refuses a negative reservation`() {
        val error = failure(snapshot(holding("10", "-1")))
        assertEquals("treasury.non-negative-reserved", (error as PlatformError.InvariantViolation).invariant)
    }

    @Test
    fun `refuses a reservation exceeding the balance, which would make available negative`() {
        val error = failure(snapshot(holding("10", "10.01")))
        assertEquals("treasury.reserved-within-total", (error as PlatformError.InvariantViolation).invariant)
    }

    @Test
    fun `refuses two holdings of the same asset, which would double count the balance`() {
        val error = failure(snapshot(holding("10", "0"), holding("5", "0")))
        assertEquals("treasury.single-holding-per-asset", (error as PlatformError.InvariantViolation).invariant)
    }

    @Test
    fun `the same asset written with a different address case still counts as one holding`() {
        val lowercased = usdcOnBase.copy(contract = address(usdcOnBase.contract!!.lowercase))
        assertTrue(snapshot(holding("10", "0"), holding("5", "0", lowercased)) is Outcome.Failure)
    }

    @Test
    fun `an empty treasury is valid, because a fresh install holds nothing`() {
        assertEquals(0, TreasurySnapshot.empty(1_000).holdings.size)
        assertTrue(snapshot() is Outcome.Success)
    }

    @Test
    fun `reserving capital reduces the available balance without changing the total`() {
        val reserved = snapshot(holding("100", "0")).expect().reserve(usdcOnBase, amount("40")).expect()
        assertEquals(amount("100"), reserved.totalOf(usdcOnBase))
        assertEquals(amount("60"), reserved.availableOf(usdcOnBase))
    }

    @Test
    fun `reserving more than is available is refused as a policy refusal, not an exception`() {
        val treasury = snapshot(holding("100", "70")).expect()
        val error = failure(treasury.reserve(usdcOnBase, amount("30.01")))
        assertEquals("treasury.insufficient-available", (error as PlatformError.PolicyRefusal).ruleId)
    }

    @Test
    fun `reserving exactly the available balance is allowed`() {
        val treasury = snapshot(holding("100", "70")).expect()
        assertEquals(Amount.ZERO, treasury.reserve(usdcOnBase, amount("30")).expect().availableOf(usdcOnBase))
    }

    @Test
    fun `a zero or negative reservation is refused`() {
        val treasury = snapshot(holding("100", "0")).expect()
        assertTrue(treasury.reserve(usdcOnBase, Amount.ZERO) is Outcome.Failure)
        assertTrue(treasury.reserve(usdcOnBase, amount("-5")) is Outcome.Failure)
    }

    @Test
    fun `reserving against an unheld asset is refused rather than creating a holding`() {
        val treasury = snapshot(holding("100", "0")).expect()
        assertTrue(treasury.reserve(ethOnBase, amount("1")) is Outcome.Failure)
    }

    @Test
    fun `reservations accumulate without losing the invariant`() {
        var treasury = snapshot(holding("10", "0")).expect()
        repeat(10) { treasury = treasury.reserve(usdcOnBase, amount("1")).expect() }
        assertEquals(Amount.ZERO, treasury.availableOf(usdcOnBase))
        assertTrue(treasury.reserve(usdcOnBase, amount("0.000001")) is Outcome.Failure)
    }
}
