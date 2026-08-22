package com.lennit.cryptolyzer.domain

import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError

/**
 * Point-in-time observed financial state.
 *
 * Treasury state is *observed and reconciled*, never inferred from application assumptions. The
 * constructor enforces the accounting invariants, so an object that violates them cannot exist
 * anywhere in the system, in any layer, in any test.
 */
public class TreasurySnapshot private constructor(
    public val takenAtEpochMillis: Long,
    public val holdings: List<Holding>,
) {
    public data class Holding(
        val asset: Asset,
        val total: Amount,
        val reserved: Amount,
    ) {
        public val available: Amount get() = total - reserved
    }

    public fun holdingOf(asset: Asset): Holding? = holdings.firstOrNull { it.asset == asset }

    public fun totalOf(asset: Asset): Amount = holdingOf(asset)?.total ?: Amount.ZERO

    public fun availableOf(asset: Asset): Amount = holdingOf(asset)?.available ?: Amount.ZERO

    /** Reserves capital for a pending action. Fails rather than over-committing. */
    public fun reserve(asset: Asset, amount: Amount): Outcome<TreasurySnapshot> {
        if (!amount.isPositive) {
            return Outcome.failure(PlatformError.Validation("Reservation must be positive"))
        }
        val holding = holdingOf(asset)
            ?: return Outcome.failure(PlatformError.Validation("No holding for ${asset.symbol}"))
        if (holding.available < amount) {
            return Outcome.failure(
                PlatformError.PolicyRefusal(
                    "Insufficient available balance: need ${amount.toPlainString()}, " +
                        "have ${holding.available.toPlainString()} ${asset.symbol}",
                    ruleId = "treasury.insufficient-available",
                ),
            )
        }
        val updated = holdings.map {
            if (it.asset == asset) it.copy(reserved = it.reserved + amount) else it
        }
        return of(takenAtEpochMillis, updated)
    }

    override fun toString(): String =
        "TreasurySnapshot(at=$takenAtEpochMillis, holdings=${holdings.size})"

    public companion object {
        /**
         * The only constructor. Enforced invariants:
         *  1. no negative total,
         *  2. no negative reservation,
         *  3. reserved never exceeds total, so available is never negative,
         *  4. one holding per asset, so balances cannot be double counted.
         */
        public fun of(takenAtEpochMillis: Long, holdings: List<Holding>): Outcome<TreasurySnapshot> {
            if (takenAtEpochMillis < 0) {
                return Outcome.failure(PlatformError.Validation("Snapshot timestamp cannot be negative"))
            }
            holdings.forEach { holding ->
                if (holding.total.isNegative) {
                    return Outcome.failure(
                        PlatformError.InvariantViolation(
                            "Negative total for ${holding.asset.symbol}",
                            invariant = "treasury.non-negative-total",
                        ),
                    )
                }
                if (holding.reserved.isNegative) {
                    return Outcome.failure(
                        PlatformError.InvariantViolation(
                            "Negative reservation for ${holding.asset.symbol}",
                            invariant = "treasury.non-negative-reserved",
                        ),
                    )
                }
                if (holding.reserved > holding.total) {
                    return Outcome.failure(
                        PlatformError.InvariantViolation(
                            "Reserved ${holding.reserved.toPlainString()} exceeds total " +
                                "${holding.total.toPlainString()} for ${holding.asset.symbol}",
                            invariant = "treasury.reserved-within-total",
                        ),
                    )
                }
            }
            val duplicates = holdings.groupBy { it.asset.canonicalId }.filterValues { it.size > 1 }
            if (duplicates.isNotEmpty()) {
                return Outcome.failure(
                    PlatformError.InvariantViolation(
                        "Duplicate holdings for ${duplicates.keys}",
                        invariant = "treasury.single-holding-per-asset",
                    ),
                )
            }
            return Outcome.success(TreasurySnapshot(takenAtEpochMillis, holdings.toList()))
        }

        public fun empty(takenAtEpochMillis: Long): TreasurySnapshot =
            TreasurySnapshot(takenAtEpochMillis, emptyList())
    }
}
