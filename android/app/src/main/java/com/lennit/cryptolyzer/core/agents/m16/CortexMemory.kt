package com.lennit.cryptolyzer.core.agents.m16

import com.lennit.cryptolyzer.core.agents.Agent
import com.lennit.cryptolyzer.core.model.AgentDescriptor
import com.lennit.cryptolyzer.core.model.AgentId
import com.lennit.cryptolyzer.core.model.AgentStatus

interface MemoryStore {
    suspend fun put(namespace: String, key: String, valueJson: String)
    suspend fun get(namespace: String, key: String): String?
}

class CortexolyzerAgent(private val memory: MemoryStore) : Agent {
    override val descriptor = AgentDescriptor(AgentId("M16"), "Cortexolyzer", AgentStatus.Ready)
    override suspend fun start() = Unit
    override suspend fun stop() = Unit

    suspend fun remember(namespace: String, key: String, valueJson: String) = memory.put(namespace, key, valueJson)
    suspend fun recall(namespace: String, key: String): String? = memory.get(namespace, key)
}
