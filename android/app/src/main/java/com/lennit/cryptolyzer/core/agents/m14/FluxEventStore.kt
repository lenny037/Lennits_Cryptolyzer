package com.lennit.cryptolyzer.core.agents.m14

import com.lennit.cryptolyzer.data.local.DomainEventDao
import com.lennit.cryptolyzer.core.agents.Agent
import com.lennit.cryptolyzer.core.model.AgentDescriptor
import com.lennit.cryptolyzer.core.model.AgentId
import com.lennit.cryptolyzer.core.model.AgentStatus
import kotlinx.coroutines.flow.Flow
import com.lennit.cryptolyzer.data.local.DomainEventEntity

class FluxolyzerAgent(private val dao: DomainEventDao) : Agent {
    override val descriptor = AgentDescriptor(AgentId("M14"), "Fluxolyzer", AgentStatus.Ready)
    override suspend fun start() = Unit
    override suspend fun stop() = Unit

    fun observeRecentEvents(limit: Int = 100): Flow<List<DomainEventEntity>> = dao.observeRecent(limit)
}
