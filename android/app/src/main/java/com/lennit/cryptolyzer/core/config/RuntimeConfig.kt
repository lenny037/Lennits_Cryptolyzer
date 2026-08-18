package com.lennit.cryptolyzer.core.config

import android.content.Context
import android.net.Uri

class RuntimeConfig private constructor(val rpcEndpoint: String?) {
    companion object {
        private const val PREFS = "cryptolyzer_runtime"
        private const val RPC_ENDPOINT = "rpc_endpoint"

        fun load(context: Context): RuntimeConfig = RuntimeConfig(
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(RPC_ENDPOINT, null)
        )

        fun saveRpcEndpoint(context: Context, endpoint: String) {
            val uri = Uri.parse(endpoint)
            require(uri.scheme == "https") { "RPC endpoint must use HTTPS" }
            require(!uri.host.isNullOrBlank()) { "RPC endpoint must include a host" }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(RPC_ENDPOINT, endpoint)
                .apply()
        }
    }
}
