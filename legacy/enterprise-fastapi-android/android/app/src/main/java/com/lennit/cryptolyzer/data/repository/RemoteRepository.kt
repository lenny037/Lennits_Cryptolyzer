package com.lennit.cryptolyzer.data.repository

import com.lennit.cryptolyzer.data.remote.LennitApiService
import com.lennit.cryptolyzer.data.remote.dto.*
import com.lennit.cryptolyzer.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MODULE 11: Remote Repository — maps API DTOs to domain models.
 */
@Singleton
class RemoteRepository @Inject constructor(
    private val api: LennitApiService,
) : CryptolyzerRepository {

    override suspend fun getDashboard(): SystemStatus = withContext(Dispatchers.IO) {
        val res = api.getDashboard()
        if (res.isSuccessful) {
            val dto = res.body()!!
            SystemStatus(
                mode          = SystemMode.FULL_AUTONOMY,
                runningAgents = dto.activeAgents,
                totalAgents   = dto.totalAgents,
                portfolioUsd  = dto.totalPortfolioUsd.replace("[$,+]".toRegex(), "").toDoubleOrNull() ?: 0.0,
                pnl24h        = dto.pnl24hUsd.replace("[$,+]".toRegex(), "").toDoubleOrNull() ?: 0.0,
                uptimeSeconds = dto.uptimeSeconds,
            )
        } else SystemStatus()
    }

    override suspend fun getAgents(): List<Agent> = withContext(Dispatchers.IO) {
        api.listAgents().body()?.map { dto ->
            Agent(
                id             = dto.id,
                name           = dto.name,
                status         = when (dto.status) {
                    "RUNNING" -> AgentStatus.RUNNING
                    "PAUSED"  -> AgentStatus.PAUSED
                    "ERROR"   -> AgentStatus.ERROR
                    else      -> AgentStatus.STOPPED
                },
                role           = when (dto.role) {
                    "ARBITRAGE"  -> AgentRole.ARBITRAGE
                    "HIMAP"      -> AgentRole.HIMAP
                    "DARKFOREST" -> AgentRole.DARKFOREST
                    else         -> AgentRole.ARBITRAGE
                },
                profitUsd      = dto.profitUsd,
                uptimeSeconds  = dto.uptimeSeconds,
            )
        } ?: emptyList()
    }

    override suspend fun getVaultPositions(): List<VaultPosition> = withContext(Dispatchers.IO) {
        api.getVault().body()?.map { dto ->
            VaultPosition(dto.symbol, dto.name, dto.amount, dto.usdValue, dto.chain, dto.apy)
        } ?: emptyList()
    }

    override suspend fun getStrategies(): List<Strategy> = withContext(Dispatchers.IO) {
        api.listStrategies().body()?.map { dto ->
            Strategy(
                id           = dto.id,
                name         = dto.name,
                type         = StrategyType.valueOf(dto.type.uppercase()),
                mode         = StrategyMode.valueOf(dto.mode.uppercase()),
                allocatedUsd = dto.allocatedUsd,
                pnl24h       = dto.pnl24h,
                winRate      = dto.winRate,
            )
        } ?: emptyList()
    }

    override suspend fun getNotifications(limit: Int): List<NotificationItem> = withContext(Dispatchers.IO) {
        api.getNotifications(limit).body()?.map { dto ->
            NotificationItem(
                id        = dto.id,
                timestamp = dto.timestamp,
                level     = NotifLevel.valueOf(dto.level.uppercase()),
                message   = dto.message,
                detail    = dto.detail,
            )
        } ?: emptyList()
    }

    override suspend fun controlAgent(agentId: String, action: String): Boolean =
        withContext(Dispatchers.IO) {
            api.controlAgent(agentId, ControlRequest(action)).isSuccessful
        }

    override suspend fun updateStrategyMode(strategyId: String, mode: String): Boolean =
        withContext(Dispatchers.IO) {
            api.updateStrategyMode(StrategyModeRequest(strategyId, mode)).isSuccessful
        }

    override suspend fun activateSafeMode(): Boolean =
        withContext(Dispatchers.IO) { api.activateSafeMode().isSuccessful }

    override suspend fun emergencyShutdown(): Boolean =
        withContext(Dispatchers.IO) { api.emergencyShutdown().isSuccessful }

    override suspend fun resumeOperations(): Boolean =
        withContext(Dispatchers.IO) { api.resumeOperations().isSuccessful }
}
