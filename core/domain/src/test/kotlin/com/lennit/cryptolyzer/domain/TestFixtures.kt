package com.lennit.cryptolyzer.domain

import com.lennit.cryptolyzer.contracts.Outcome

/** Unwraps in tests only: a failure here should fail the test loudly with the real error. */
internal fun <T> Outcome<T>.expect(): T = when (this) {
    is Outcome.Success -> value
    is Outcome.Failure -> throw AssertionError("expected success but got: ${error.message}")
}

internal fun amount(text: String): Amount = Amount.parse(text).expect()

internal fun address(text: String): EvmAddress = EvmAddress.parse(text).expect()

internal val usdcOnBase: Asset = Asset(
    chainId = ChainId.BASE,
    symbol = "USDC",
    decimals = 6,
    contract = address("0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913"),
)

internal val ethOnBase: Asset = Asset(chainId = ChainId.BASE, symbol = "ETH", decimals = 18)
