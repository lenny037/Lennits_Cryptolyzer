package com.lennit.cryptolyzer.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StatusDto(
    val mode:           String,
    @SerializedName("running_agents") val runningAgents: Int,
    @SerializedName("last_updated")   val lastUpdated:   String,
    val version:        String,
)

data class DashboardDto(
    @SerializedName("total_portfolio_usd") val totalPortfolioUsd: String,
    @SerializedName("pnl_24h_usd")        val pnl24hUsd:         String,
    @SerializedName("pnl_24h_pct")        val pnl24hPct:         String,
    @SerializedName("active_agents")      val activeAgents:      Int,
    @SerializedName("total_agents")       val totalAgents:       Int,
    @SerializedName("system_status")      val systemStatus:      String,
    @SerializedName("uptime_seconds")     val uptimeSeconds:     Long,
)

data class PortfolioMetricsDto(
    @SerializedName("total_value_usd")  val totalValueUsd:  Double,
    @SerializedName("allocated_usd")    val allocatedUsd:   Double,
    @SerializedName("available_usd")    val availableUsd:   Double,
    @SerializedName("daily_pnl")        val dailyPnl:       Double,
    @SerializedName("weekly_pnl")       val weeklyPnl:      Double,
    @SerializedName("monthly_pnl")      val monthlyPnl:     Double,
    @SerializedName("drawdown_pct")     val drawdownPct:    Double,
    @SerializedName("sharpe_ratio")     val sharpeRatio:    Double,
)

data class AgentDto(
    val id:         String,
    val name:       String,
    val status:     String,
    val role:       String,
    @SerializedName("profit_usd")   val profitUsd:   Double = 0.0,
    @SerializedName("uptime_seconds") val uptimeSeconds: Long = 0L,
)

data class ControlRequest(val action: String, val mode: String? = null)

data class VaultPositionDto(
    val symbol:     String,
    val name:       String,
    val amount:     Double,
    @SerializedName("usd_value") val usdValue: Double,
    val chain:      String = "ETH",
    val apy:        Double? = null,
)

data class StrategyDto(
    val id:              String,
    val name:            String,
    val type:            String,
    val mode:            String,
    @SerializedName("allocated_usd") val allocatedUsd: Double = 0.0,
    @SerializedName("pnl_24h")      val pnl24h:       Double = 0.0,
    @SerializedName("win_rate")     val winRate:      Double = 0.0,
)

data class StrategyModeRequest(
    @SerializedName("strategy_id") val strategyId: String,
    val mode: String,
)

data class NotificationDto(
    val id:        String,
    val timestamp: String,
    val level:     String,
    val message:   String,
    val detail:    String = "",
)

data class SignalDto(
    val symbol:     String,
    val signal:     String,
    val confidence: Double,
    val price:      Double,
    val timestamp:  String,
)

data class GasEstimateDto(
    val chain:       String,
    val gwei:        Double,
    @SerializedName("usd_cost")   val usdCost:   Double,
    val recommended: String,
)

data class TradeRequest(
    val symbol:     String,
    val side:       String,
    @SerializedName("amount_usd") val amountUsd: Double,
    @SerializedName("slippage_tolerance") val slippageTolerance: Double = 0.005,
)
