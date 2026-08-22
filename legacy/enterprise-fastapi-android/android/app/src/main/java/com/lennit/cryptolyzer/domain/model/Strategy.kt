package com.lennit.cryptolyzer.domain.model

data class Strategy(
    val id:           String,
    val name:         String,
    val type:         StrategyType,
    val mode:         StrategyMode,
    val allocatedUsd: Double = 0.0,
    val pnl24h:       Double = 0.0,
    val winRate:      Double = 0.0,
)

enum class StrategyType { ARBITRAGE, YIELD, AIRDROP, MEV, CROSS_CHAIN }
enum class StrategyMode { LIVE, SHADOW, OFF }
