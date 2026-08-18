package com.lennit.cryptolyzer.core.agents

import com.lennit.cryptolyzer.core.model.AgentDescriptor
import com.lennit.cryptolyzer.core.model.AgentId
import com.lennit.cryptolyzer.core.model.AgentStatus

interface Agent {
    val descriptor: AgentDescriptor
    suspend fun start()
    suspend fun stop()
}

class AgentRuntime(private val agents: List<Agent>) {
    fun descriptors(): List<AgentDescriptor> = agents.map { it.descriptor }

    suspend fun startAll(): List<AgentDescriptor> {
        agents.forEach { it.start() }
        return descriptors()
    }

    suspend fun stopAll() = agents.forEach { it.stop() }
}

class DisabledAgent(id: String, name: String) : Agent {
    override val descriptor = AgentDescriptor(AgentId(id), name, AgentStatus.Disabled)
    override suspend fun start() = Unit
    override suspend fun stop() = Unit
}
