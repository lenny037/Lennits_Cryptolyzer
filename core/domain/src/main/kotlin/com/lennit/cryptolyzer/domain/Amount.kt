package com.lennit.cryptolyzer.domain

import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

/**
 * Exact decimal quantity.
 *
 * Floating point is banned from every financial path in this platform. The legacy Python and
 * TypeScript material used doubles for balances, profit and EV, which silently loses precision at
 * 18-decimal token scale and makes accounting invariants unprovable. [Amount] wraps
 * [BigDecimal] and refuses to expose any lossy conversion.
 */
public class Amount private constructor(public val value: BigDecimal) : Comparable<Amount> {

    public val isZero: Boolean get() = value.signum() == 0
    public val isNegative: Boolean get() = value.signum() < 0
    public val isPositive: Boolean get() = value.signum() > 0

    public operator fun plus(other: Amount): Amount = Amount(value.add(other.value))

    public operator fun minus(other: Amount): Amount = Amount(value.subtract(other.value))

    public operator fun times(factor: BigDecimal): Amount = Amount(value.multiply(factor))

    /** Division requires an explicit scale and rounding mode: no implicit truncation. */
    public fun divide(divisor: Amount, scale: Int, rounding: RoundingMode): Outcome<Amount> =
        if (divisor.isZero) {
            Outcome.failure(PlatformError.Validation("Division by zero amount"))
        } else {
            Outcome.success(Amount(value.divide(divisor.value, scale, rounding)))
        }

    public fun ratioTo(total: Amount): Outcome<BigDecimal> =
        if (total.isZero) {
            Outcome.failure(PlatformError.Validation("Cannot compute ratio against zero total"))
        } else {
            Outcome.success(value.divide(total.value, MathContext.DECIMAL64))
        }

    public fun negate(): Amount = Amount(value.negate())

    public fun abs(): Amount = Amount(value.abs())

    /** Renders with a fixed scale for display and for stable serialization. */
    public fun toPlainString(scale: Int = value.scale()): String =
        value.setScale(scale, RoundingMode.DOWN).toPlainString()

    /**
     * Converts to the chain-native integer representation, for example wei.
     * Rejects any value that would lose precision instead of rounding it away.
     */
    public fun toBaseUnits(decimals: Int): Outcome<BigInteger> {
        require(decimals >= 0) { "decimals cannot be negative" }
        val scaled = value.movePointRight(decimals)
        return if (scaled.stripTrailingZeros().scale() > 0) {
            Outcome.failure(
                PlatformError.Validation(
                    "Amount ${value.toPlainString()} cannot be represented in $decimals decimals without loss",
                ),
            )
        } else {
            Outcome.success(scaled.toBigIntegerExact())
        }
    }

    override fun compareTo(other: Amount): Int = value.compareTo(other.value)

    /** Value equality is numeric: 1.50 equals 1.5. */
    override fun equals(other: Any?): Boolean =
        this === other || (other is Amount && value.compareTo(other.value) == 0)

    override fun hashCode(): Int = value.stripTrailingZeros().hashCode()

    override fun toString(): String = "Amount(${value.toPlainString()})"

    public companion object {
        public val ZERO: Amount = Amount(BigDecimal.ZERO)

        public fun of(value: BigDecimal): Amount = Amount(value)

        public fun of(value: Long): Amount = Amount(BigDecimal.valueOf(value))

        /** Parses a decimal string. The only accepted textual entry point. */
        public fun parse(text: String): Outcome<Amount> =
            runCatching { BigDecimal(text.trim()) }
                .fold(
                    onSuccess = { Outcome.success(Amount(it)) },
                    onFailure = { Outcome.failure(PlatformError.Validation("Not a decimal number: '$text'")) },
                )

        /** Builds from chain-native integer units, for example wei to ether. */
        public fun fromBaseUnits(units: BigInteger, decimals: Int): Amount {
            require(decimals >= 0) { "decimals cannot be negative" }
            return Amount(BigDecimal(units).movePointLeft(decimals))
        }
    }
}
