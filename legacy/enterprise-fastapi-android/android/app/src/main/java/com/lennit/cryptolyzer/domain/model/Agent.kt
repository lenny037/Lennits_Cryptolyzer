package com.lennit.cryptolyzer.domain.model

data class Agent(
    val id:              String,
    val name:            String,
    val status:          AgentStatus,
    val role:            AgentRole,
    val profitUsd:       Double = 0.0,
    val uptimeSeconds:   Long   = 0L,
    val tradesExecuted:  Int    = 0,
    val lastAction:      String = "idle",
)

enum class AgentStatus { RUNNING, PAUSED, ERROR, STOPPED }
enum class AgentRole   { ARBITRAGE, HIMAP, DARKFOREST }
