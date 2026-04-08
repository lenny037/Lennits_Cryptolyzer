package com.lennit.cryptolyzer.domain

enum class AgentStatus { RUNNING, PAUSED, ERROR }

data class Agent(
    val id: String,
    val name: String,
    val status: AgentStatus
)

data class VaultPosition(
    val symbol: String,
    val chain: String,
    val balance: Double,
    val usdValue: Double
)

enum class StrategyType { ARBITRAGE, YIELD, AIRDROP }

enum class StrategyMode { LIVE, SHADOW, OFF }

data class StrategyConfig(
    val id: String,
    val name: String,
    val type: StrategyType,
    val mode: StrategyMode
)

enum class NotificationSeverity { INFO, WARNING, CRITICAL }

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val severity: NotificationSeverity,
    val isRead: Boolean
)

enum class SystemStatus { OK, SHUTDOWN }
