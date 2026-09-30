package com.lennit.cryptolyzer.blockchain

data class ChainRef(val chainId: Long, val name: String)

data class BlockObservation(
    val chain: ChainRef,
    val blockNumber: Long,
    val observedAtEpochMs: Long
)

interface BlockchainObserver {
    suspend fun latestBlock(chain: ChainRef): BlockObservation
    suspend fun balance(chain: ChainRef, address: String): String
    suspend fun simulate(chain: ChainRef, request: SimulationRequest): SimulationResult
}

data class SimulationRequest(val to: String, val data: String, val value: String)
data class SimulationResult(val success: Boolean, val gasEstimate: String?, val error: String?)
