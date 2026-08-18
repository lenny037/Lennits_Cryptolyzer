package com.lennit.cryptolyzer.core.agents.m00

import com.lennit.cryptolyzer.core.model.AgentDescriptor
import com.lennit.cryptolyzer.core.model.AgentId
import com.lennit.cryptolyzer.core.model.AgentStatus
import com.lennit.cryptolyzer.core.model.MarketSignal
import com.lennit.cryptolyzer.core.agents.Agent

interface SignalSource {
    suspend fun collect(): List<MarketSignal>
}

class ExtractorAgent(private val sources: List<SignalSource>) : Agent {
    override val descriptor = AgentDescriptor(AgentId("M00"), "Extractor", AgentStatus.Ready)

    override suspend fun start() = Unit
    override suspend fun stop() = Unit

    suspend fun extract(): List<MarketSignal> = sources.flatMap { source -> source.collect() }
}
