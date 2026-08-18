package com.lennit.cryptolyzer.core.agents.m08

import org.json.JSONObject

class EvmBlockchainGateway(private val rpc: JsonRpcClient) : BlockchainGateway {
    override suspend fun latestBlock(): Long {
        val result = rpc.call("eth_blockNumber")
        val hex = JSONObject(result).getString("result")
        return hex.removePrefix("0x").toLong(16)
    }

    override suspend fun getBalance(address: String): String {
        require(address.matches(Regex("0x[0-9a-fA-F]{40}"))) { "Invalid EVM address" }
        val result = rpc.call("eth_getBalance", "[\"$address\",\"latest\"]")
        return JSONObject(result).getString("result")
    }

    override suspend fun simulateCall(to: String, dataHex: String, valueWei: String): SimulationResult {
        require(to.matches(Regex("0x[0-9a-fA-F]{40}"))) { "Invalid EVM destination" }
        require(dataHex.matches(Regex("0x[0-9a-fA-F]*"))) { "Invalid calldata" }
        require(valueWei.matches(Regex("0x[0-9a-fA-F]+|[0-9]+"))) { "Invalid value" }
        return try {
            val value = if (valueWei.startsWith("0x")) valueWei else "0x${valueWei.toLong().toString(16)}"
            val result = rpc.call("eth_estimateGas", "[{\"to\":\"$to\",\"data\":\"$dataHex\",\"value\":\"$value\"}]")
            val json = JSONObject(result)
            if (json.has("error")) SimulationResult(false, revertReason = json.getJSONObject("error").optString("message"))
            else SimulationResult(true, gasEstimate = json.getString("result").removePrefix("0x").toLong(16))
        } catch (e: Exception) {
            SimulationResult(false, revertReason = e.message ?: "RPC simulation failed")
        }
    }
}
