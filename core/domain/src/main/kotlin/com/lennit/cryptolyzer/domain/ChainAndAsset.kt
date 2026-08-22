package com.lennit.cryptolyzer.domain

import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError

/** EVM chain identifier. Kept as a plain id so new chains need no code change. */
@JvmInline
public value class ChainId(public val numeric: Long) {
    init {
        require(numeric > 0) { "Chain id must be positive" }
    }

    override fun toString(): String = "chain:$numeric"

    public companion object {
        public val ETHEREUM: ChainId = ChainId(1)
        public val BASE: ChainId = ChainId(8453)
        public val OPTIMISM: ChainId = ChainId(10)
        public val ARBITRUM_ONE: ChainId = ChainId(42161)
        public val POLYGON: ChainId = ChainId(137)
    }
}

/**
 * A validated EVM address in EIP-55 checksum form.
 *
 * Validation happens once, at the boundary, so no downstream code has to guess whether a
 * string is an address. A mixed-case input with a bad checksum is rejected outright: silently
 * lowercasing it is how funds get sent to a typo.
 */
public class EvmAddress private constructor(public val checksummed: String) {

    public val lowercase: String get() = checksummed.lowercase()

    override fun equals(other: Any?): Boolean =
        this === other || (other is EvmAddress && lowercase == other.lowercase)

    override fun hashCode(): Int = lowercase.hashCode()

    override fun toString(): String = checksummed

    public companion object {
        private val HEX = Regex("^0x[0-9a-fA-F]{40}$")

        public fun parse(raw: String): Outcome<EvmAddress> {
            val text = raw.trim()
            if (!HEX.matches(text)) {
                return Outcome.failure(PlatformError.Validation("Not a 20-byte hex address: '$raw'"))
            }
            val body = text.substring(2)
            val isMixedCase = body != body.lowercase() && body != body.uppercase()
            val checksum = toChecksum(body.lowercase())
            if (isMixedCase && body != checksum) {
                return Outcome.failure(
                    PlatformError.Validation("EIP-55 checksum mismatch for address '$raw'"),
                )
            }
            return Outcome.success(EvmAddress("0x$checksum"))
        }

        private fun toChecksum(lowerBody: String): String {
            val hash = Keccak256.digest(lowerBody.toByteArray(Charsets.US_ASCII))
            val hex = hash.joinToString("") { byte -> "%02x".format(byte) }
            return buildString(lowerBody.length) {
                lowerBody.forEachIndexed { index, char ->
                    val nibble = hex[index].digitToInt(16)
                    append(if (char in 'a'..'f' && nibble >= 8) char.uppercaseChar() else char)
                }
            }
        }
    }
}

/** A tradeable or holdable asset, identified by chain plus contract, or chain plus native flag. */
public data class Asset(
    val chainId: ChainId,
    val symbol: String,
    val decimals: Int,
    val contract: EvmAddress? = null,
) {
    init {
        require(symbol.isNotBlank()) { "Asset symbol cannot be blank" }
        require(decimals in 0..36) { "Implausible decimals: $decimals" }
    }

    public val isNative: Boolean get() = contract == null

    public val canonicalId: String
        get() = "${chainId.numeric}/${contract?.lowercase ?: "native"}"
}

/** A quantity of a specific asset. Arithmetic across different assets is a compile-time error path. */
public data class AssetAmount(val asset: Asset, val amount: Amount) {

    public operator fun plus(other: AssetAmount): Outcome<AssetAmount> =
        if (other.asset != asset) {
            Outcome.failure(
                PlatformError.InvariantViolation(
                    "Cannot add ${other.asset.symbol} to ${asset.symbol}",
                    invariant = "asset-homogeneity",
                ),
            )
        } else {
            Outcome.success(copy(amount = amount + other.amount))
        }

    override fun toString(): String = "${amount.toPlainString()} ${asset.symbol}"
}
