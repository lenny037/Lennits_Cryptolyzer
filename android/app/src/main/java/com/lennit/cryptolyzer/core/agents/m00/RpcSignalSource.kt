package com.lennit.cryptolyzer.core.agents.m00

import com.lennit.cryptolyzer.core.agents.m08.JsonRpcClient
import com.lennit.cryptolyzer.core.model.MarketSignal
import org.json.JSONObject

class RpcSignalSource(private val rpc: JsonRpcClient) : SignalSource {
    override suspend fun collect(): List<MarketSignal> {
        val response = rpc.call("eth_blockNumber")
        val block = JSONObject(response).getString("result")
        return listOf(
            MarketSignal(
                source = "evm-rpc",
                symbol = "BLOCK",
                observedAtEpochMs = System.currentTimeMillis(),
                confidence = 1.0,
                payloadJson = "{\"blockNumber\":\"$block\"}"
            )
        )
    }
}
