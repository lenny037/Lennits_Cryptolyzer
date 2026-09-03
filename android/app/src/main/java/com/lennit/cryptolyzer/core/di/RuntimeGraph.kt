package com.lennit.cryptolyzer.core.di

import android.content.Context
import com.lennit.cryptolyzer.core.agents.m00.ExtractorAgent
import com.lennit.cryptolyzer.core.agents.m00.RpcSignalSource
import com.lennit.cryptolyzer.core.agents.m08.EvmBlockchainGateway
import com.lennit.cryptolyzer.core.agents.m08.JsonRpcClient
import com.lennit.cryptolyzer.core.agents.m12.SentinelAgent
import com.lennit.cryptolyzer.core.agents.m16.CortexolyzerAgent
import com.lennit.cryptolyzer.core.memory.RoomMemoryStore
import com.lennit.cryptolyzer.core.orchestration.Orchestralyzer
import com.lennit.cryptolyzer.core.security.ExecutionPolicy
import com.lennit.cryptolyzer.data.local.DatabaseProvider
import java.net.URI

class RuntimeGraph(context: Context, rpcEndpoint: URI) {
    private val database = DatabaseProvider.get(context)
    private val rpc = JsonRpcClient(rpcEndpoint)

    val extractor = ExtractorAgent(listOf(RpcSignalSource(rpc)))
    val chainsighter = EvmBlockchainGateway(rpc)
    val sentinel = SentinelAgent(emptyList())
    val cortex = CortexolyzerAgent(RoomMemoryStore(database.agentMemoryDao()))
    val policy = ExecutionPolicy()

    val orchestralyzer = Orchestralyzer(
        extractor = extractor,
        chainsighter = chainsighter,
        sentinel = sentinel,
        cortex = cortex,
        policy = policy
    )
}
