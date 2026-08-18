package com.lennit.cryptolyzer.core.modules

import com.lennit.cryptolyzer.core.model.AgentStatus

interface CryptolyzerModule {
    val id: String
    val name: String
    val status: AgentStatus
    suspend fun initialize()
    suspend fun shutdown()
}

data class ModuleHealth(
    val id: String,
    val ready: Boolean,
    val detail: String? = null
)
