package com.lennit.cryptolyzer.data.websocket

import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

data class MetricsPayload(
    val type:           String  = "metrics",
    val timestamp:      String  = "",
    val portfolioUsd:   Double  = 0.0,
    val pnl24h:         Double  = 0.0,
    val activeAgents:   Int     = 0,
    val btcPrice:       Double  = 0.0,
    val ethPrice:       Double  = 0.0,
    val gasGwei:        Double  = 0.0,
)

/**
 * MODULE 11: OkHttp WebSocket client for live metrics streaming.
 */
@Singleton
class MetricsWebSocket @Inject constructor(private val client: OkHttpClient) {

    private val gson = Gson()

    fun observe(wsUrl: String): Flow<MetricsPayload> = callbackFlow {
        val request = Request.Builder().url(wsUrl).build()
        val listener = object : WebSocketListener() {
            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val payload = gson.fromJson(text, MetricsPayload::class.java)
                    trySend(payload)
                } catch (_: Exception) {}
            }
            override fun onFailure(ws: WebSocket, t: Throwable, r: Response?) {
                close(t)
            }
        }
        val socket = client.newWebSocket(request, listener)
        awaitClose { socket.cancel() }
    }
}
