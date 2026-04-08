package com.lennit.cryptolyzer.data.repository

import com.lennit.cryptolyzer.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AgentsRepository {
    val agents: StateFlow<List<Agent>>
    fun toggleAgent(id: String)
}

class InMemoryAgentsRepository : AgentsRepository {
    private val _agents = MutableStateFlow(
        listOf(
            Agent("1", "BTC Market Maker", AgentStatus.RUNNING),
            Agent("2", "ETH Yield Harvester", AgentStatus.PAUSED),
            Agent("3", "SOL Airdrop Sniper", AgentStatus.ERROR),
        )
    )
    override val agents: StateFlow<List<Agent>> = _agents.asStateFlow()

    override fun toggleAgent(id: String) {
        _agents.value = _agents.value.map {
            if (it.id == id) {
                val newStatus = when (it.status) {
                    AgentStatus.RUNNING -> AgentStatus.PAUSED
                    AgentStatus.PAUSED, AgentStatus.ERROR -> AgentStatus.RUNNING
                }
                it.copy(status = newStatus)
            } else it
        }
    }
}

interface VaultRepository {
    val positions: StateFlow<List<VaultPosition>>
}

class InMemoryVaultRepository : VaultRepository {
    private val _positions = MutableStateFlow(
        listOf(
            VaultPosition("BTC", "Bitcoin", 1.234, 85000.0),
            VaultPosition("ETH", "Ethereum", 12.0, 42000.0),
            VaultPosition("SOL", "Solana", 350.0, 28000.0),
        )
    )
    override val positions: StateFlow<List<VaultPosition>> = _positions.asStateFlow()
}

interface StrategiesRepository {
    val strategies: StateFlow<List<StrategyConfig>>
    fun updateMode(id: String, mode: StrategyMode)
}

class InMemoryStrategiesRepository : StrategiesRepository {
    private val _strategies = MutableStateFlow(
        listOf(
            StrategyConfig("1", "BTC Perp Tri-Arb", StrategyType.ARBITRAGE, StrategyMode.LIVE),
            StrategyConfig("2", "Stablecoin Farming", StrategyType.YIELD, StrategyMode.SHADOW),
            StrategyConfig("3", "Retroactive Airdrops", StrategyType.AIRDROP, StrategyMode.OFF),
        )
    )
    override val strategies: StateFlow<List<StrategyConfig>> = _strategies.asStateFlow()

    override fun updateMode(id: String, mode: StrategyMode) {
        _strategies.value = _strategies.value.map { if (it.id == id) it.copy(mode = mode) else it }
    }
}

interface NotificationsRepository {
    val notifications: StateFlow<List<NotificationItem>>
    fun markAsRead(id: String)
}

class InMemoryNotificationsRepository : NotificationsRepository {
    private val _notifications = MutableStateFlow(
        listOf(
            NotificationItem("1", "System Online", "All agents nominal.", NotificationSeverity.INFO, true),
            NotificationItem("2", "Vault Rebalance", "Rebalanced BTC/ETH weight.", NotificationSeverity.WARNING, false),
            NotificationItem("3", "Safe Shutdown", "System was shut down at 03:21 UTC.", NotificationSeverity.CRITICAL, false),
        )
    )
    override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    override fun markAsRead(id: String) {
        _notifications.value = _notifications.value.map { if (it.id == id) it.copy(isRead = true) else it }
    }
}

interface SystemRepository {
    val status: StateFlow<SystemStatus>
    fun updateStatus(status: SystemStatus)
}

class InMemorySystemRepository : SystemRepository {
    private val _status = MutableStateFlow(SystemStatus.OK)
    override val status: StateFlow<SystemStatus> = _status.asStateFlow()
    override fun updateStatus(status: SystemStatus) { _status.value = status }
}
