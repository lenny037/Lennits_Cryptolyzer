package com.lennit.cryptolyzer.domain.model

data class SystemStatus(
    val mode:           SystemMode = SystemMode.FULL_AUTONOMY,
    val runningAgents:  Int    = 0,
    val totalAgents:    Int    = 20,
    val portfolioUsd:   Double = 0.0,
    val pnl24h:         Double = 0.0,
    val uptimeSeconds:  Long   = 0L,
)

enum class SystemMode { FULL_AUTONOMY, SAFE_MODE, SHUTDOWN, DEGRADED }
