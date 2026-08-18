package com.lennit.cryptolyzer.core.runtime

import android.content.Context
import com.lennit.cryptolyzer.core.config.RuntimeConfig
import com.lennit.cryptolyzer.core.di.RuntimeGraph
import java.net.URI

class CryptolyzerRuntime(context: Context) {
    private val appContext = context.applicationContext
    private var graph: RuntimeGraph? = null

    fun configureRpc(endpoint: String) {
        RuntimeConfig.saveRpcEndpoint(appContext, endpoint)
        graph = RuntimeGraph(appContext, URI(endpoint))
    }

    fun isConfigured(): Boolean = graph != null || RuntimeConfig.load(appContext).rpcEndpoint != null

    fun startConfigured(): Boolean {
        val endpoint = RuntimeConfig.load(appContext).rpcEndpoint ?: return false
        graph = RuntimeGraph(appContext, URI(endpoint))
        return true
    }
}
