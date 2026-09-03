package com.lennit.cryptolyzer.core.agents.m08

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

class JsonRpcClient(private val endpoint: URI) {
    suspend fun call(method: String, paramsJson: String = "[]"): String = withContext(Dispatchers.IO) {
        val connection = endpoint.toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            val body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"$method\",\"params\":$paramsJson}"
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream.bufferedReader().use { it.readText() }
            check(code in 200..299) { "RPC HTTP $code" }
            response
        } finally {
            connection.disconnect()
        }
    }
}
