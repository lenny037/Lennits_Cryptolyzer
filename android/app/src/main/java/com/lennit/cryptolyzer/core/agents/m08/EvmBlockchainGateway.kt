package com.lennit.cryptolyzer.core.agents.m08

import java.math.BigInteger
import org.json.JSONObject

class EvmBlockchainGateway(private val rpc: JsonRpcClient) : BlockchainGateway {
    override suspend fun latestBlock(): Long {
        val result = rpc.call("eth_blockNumber")
        val hex = JSONObject(result).getString("result")
        return parseHexUnsigned(hex).longValueExact()
    }

    override suspend fun getBalance(address: String): String {
        require(EVM_ADDRESS.matches(address)) { "Invalid EVM address" }
        val result = rpc.call("eth_getBalance", "[\"$address\",\"latest\"]")
        val json = JSONObject(result)
        require(!json.has("error")) { json.optJSONObject("error")?.optString("message") ?: "RPC error" }
        return parseHexUnsigned(json.getString("result")).toString(10)
    }

    override suspend fun simulateCall(to: String, dataHex: String, valueWei: String): SimulationResult {
        require(EVM_ADDRESS.matches(to)) { "Invalid EVM destination" }
        require(HEX.matches(dataHex)) { "Invalid calldata" }
        val value = parseUnsigned(valueWei)
        require(value >= BigInteger.ZERO && value <= UINT256_MAX) { "EVM value exceeds uint256" }
        return try {
            val result = rpc.call(
                "eth_estimateGas",
                "[{\"to\":\"$to\",\"data\":\"$dataHex\",\"value\":\"0x${value.toString(16)}\"}]"
            )
            val json = JSONObject(result)
            if (json.has("error")) {
                SimulationResult(false, revertReason = json.getJSONObject("error").optString("message"))
            } else {
                SimulationResult(true, gasEstimate = parseHexUnsigned(json.getString("result")).longValueExact())
            }
        } catch (e: ArithmeticException) {
            SimulationResult(false, revertReason = "Gas estimate exceeds supported range")
        } catch (e: Exception) {
            SimulationResult(false, revertReason = e.message ?: "RPC simulation failed")
        }
    }

    private fun parseUnsigned(value: String): BigInteger =
        if (value.startsWith("0x", ignoreCase = true)) parseHexUnsigned(value) else BigInteger(value, 10)

    private fun parseHexUnsigned(value: String): BigInteger {
        require(value.matches(HEX)) { "Invalid hexadecimal quantity" }
        return BigInteger(value.removePrefix("0x").removePrefix("0X").ifEmpty { "0" }, 16)
    }

    companion object {
        private val EVM_ADDRESS = Regex("0x[0-9a-fA-F]{40}")
        private val HEX = Regex("0x[0-9a-fA-F]*")
        private val UINT256_MAX = BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE)
    }
}
