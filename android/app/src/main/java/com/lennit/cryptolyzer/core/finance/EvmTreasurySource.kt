package com.lennit.cryptolyzer.core.finance

import com.lennit.cryptolyzer.core.agents.m08.BlockchainGateway

class EvmTreasurySource(
    private val gateway: BlockchainGateway,
    private val address: String,
    private val nativeUsdPrice: suspend () -> Double
) : TreasurySource {
    override suspend fun totalValueUsd(): Double = valueUsd()

    override suspend fun availableValueUsd(): Double = valueUsd()

    private suspend fun valueUsd(): Double {
        val wei = gateway.getBalance(address).toBigDecimal()
        val eth = wei.movePointLeft(18)
        val price = nativeUsdPrice()
        require(price.isFinite() && price >= 0.0) { "Invalid native asset USD price" }
        return eth.toDouble() * price
    }
}
