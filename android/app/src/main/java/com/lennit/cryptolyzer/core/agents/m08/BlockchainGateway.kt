package com.lennit.cryptolyzer.core.agents.m08

import com.lennit.cryptolyzer.core.model.AgentDescriptor
import com.lennit.cryptolyzer.core.model.AgentId
import com.lennit.cryptolyzer.core.model.AgentStatus
import com.lennit.cryptolyzer.core.agents.Agent

interface BlockchainGateway {
    suspend fun latestBlock(): Long
    suspend fun getBalance(address: String): String
    suspend fun simulateCall(to: String, dataHex: String, valueWei: String): SimulationResult
}

data class SimulationResult(
    val success: Boolean,
    val gasEstimate: Long? = null,
    val revertReason: String? = null
)

class ChainsighterAgent : Agent {
    override val descriptor = AgentDescriptor(AgentId("M08"), "Chainsighter", AgentStatus.Ready)
    override suspend fun start() = Unit
    override suspend fun stop() = Unit
}
