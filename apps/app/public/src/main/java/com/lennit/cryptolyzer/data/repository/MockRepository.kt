package com.lennit.cryptolyzer.data.repository

import com.lennit.cryptolyzer.domain.model.*
import com.lennit.cryptolyzer.domain.repository.CryptolyzerRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * MODULE 11: Mock Repository for offline/demo mode.
 */
@Singleton
class MockRepository @Inject constructor() : CryptolyzerRepository {

    override suspend fun getDashboard(): SystemStatus {
        delay(300)
        return SystemStatus(
            mode          = SystemMode.FULL_AUTONOMY,
            runningAgents = 20,
            totalAgents   = 20,
            portfolioUsd  = 155_000.0 + Random.nextDouble(-500.0, 500.0),
            pnl24h        = 2_340.0   + Random.nextDouble(-100.0, 100.0),
            uptimeSeconds = 86_400L,
        )
    }

    override suspend fun getAgents(): List<Agent> {
        delay(200)
        return (1..20).map { i ->
            Agent(
                id            = "Lennit_${i.toString().padStart(2, '0')}",
                name          = "Agent ${i.toString().padStart(2, '0')}",
                status        = AgentStatus.RUNNING,
                role          = when {
                    i <= 5  -> AgentRole.ARBITRAGE
                    i <= 15 -> AgentRole.HIMAP
                    else    -> AgentRole.DARKFOREST
                },
                profitUsd     = i * 12.5 + Random.nextDouble(-5.0, 5.0),
                uptimeSeconds = 86_400L + i * 100L,
                tradesExecuted = i * 3,
                lastAction    = "scanning",
            )
        }
    }

    override suspend fun getVaultPositions(): List<VaultPosition> {
        delay(150)
        return listOf(
            VaultPosition("BTC",   "Bitcoin",   1.234,    85_000.0, "BTC"),
            VaultPosition("ETH",   "Ethereum",  12.5,     42_000.0, "ETH", apy = 4.2),
            VaultPosition("SOL",   "Solana",    350.0,    14_000.0, "SOL", apy = 7.1),
            VaultPosition("MATIC", "Polygon",   8_000.0,  5_600.0, "POLYGON", apy = 5.8),
            VaultPosition("BNB",   "BNB Chain", 22.0,     8_400.0, "BSC",     apy = 6.3),
        )
    }

    override suspend fun getStrategies(): List<Strategy> {
        delay(150)
        return listOf(
            Strategy("1", "BTC Perp Tri-Arb",      StrategyType.ARBITRAGE,   StrategyMode.LIVE,   30_000.0, 1_200.0, 0.68),
            Strategy("2", "ETH/USDC LP Farming",   StrategyType.YIELD,       StrategyMode.LIVE,   25_000.0, 320.0,   0.92),
            Strategy("3", "Retroactive Airdrops",  StrategyType.AIRDROP,     StrategyMode.SHADOW, 5_000.0,  150.0,   0.45),
            Strategy("4", "Cross-chain Flash Arb", StrategyType.CROSS_CHAIN, StrategyMode.LIVE,   20_000.0, 870.0,   0.71),
            Strategy("5", "SOL Yield Optimizer",   StrategyType.YIELD,       StrategyMode.SHADOW, 10_000.0, 280.0,   0.85),
            Strategy("6", "MEV Sandwich Guard",    StrategyType.MEV,         StrategyMode.LIVE,   15_000.0, 500.0,   0.78),
        )
    }

    override suspend fun getNotifications(limit: Int): List<NotificationItem> {
        delay(100)
        return listOf(
            NotificationItem("1","2026-04-03T08:00:00Z", NotifLevel.PROFIT, "BTC Tri-Arb: +\$1,200", "Spread: 0.42%"),
            NotificationItem("2","2026-04-03T07:45:00Z", NotifLevel.PROFIT, "ETH LP yield: +0.15 ETH","Pool: Uniswap V3"),
            NotificationItem("3","2026-04-03T07:30:00Z", NotifLevel.INFO,   "AlphaGrid: 20/20 active","Uptime: 24h"),
            NotificationItem("4","2026-04-03T07:15:00Z", NotifLevel.WARN,   "Gas spike on ETH",       "65 gwei"),
            NotificationItem("5","2026-04-03T07:00:00Z", NotifLevel.PROFIT, "Flash arb: +\$870",      "3-hop bridge"),
            NotificationItem("6","2026-04-03T06:45:00Z", NotifLevel.ALERT,  "Drawdown: -3.2%",        "Max: -5.0%"),
        ).take(limit)
    }

    override suspend fun controlAgent(agentId: String, action: String): Boolean { delay(200); return true }
    override suspend fun updateStrategyMode(strategyId: String, mode: String): Boolean { delay(200); return true }
    override suspend fun activateSafeMode(): Boolean { delay(300); return true }
    override suspend fun emergencyShutdown(): Boolean { delay(300); return true }
    override suspend fun resumeOperations(): Boolean { delay(300); return true }
}
