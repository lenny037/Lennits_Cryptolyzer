package com.lennit.cryptolyzer.domain.repository

import com.lennit.cryptolyzer.domain.model.*

interface CryptolyzerRepository {
    suspend fun getDashboard(): SystemStatus
    suspend fun getAgents(): List<Agent>
    suspend fun getVaultPositions(): List<VaultPosition>
    suspend fun getStrategies(): List<Strategy>
    suspend fun getNotifications(limit: Int = 50): List<NotificationItem>
    suspend fun controlAgent(agentId: String, action: String): Boolean
    suspend fun updateStrategyMode(strategyId: String, mode: String): Boolean
    suspend fun activateSafeMode(): Boolean
    suspend fun emergencyShutdown(): Boolean
    suspend fun resumeOperations(): Boolean
}
