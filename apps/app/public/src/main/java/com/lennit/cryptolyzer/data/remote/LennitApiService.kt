package com.lennit.cryptolyzer.data.remote

import com.lennit.cryptolyzer.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * MODULE 11: Retrofit service interface — full API contract.
 */
interface LennitApiService {

    // ── System ─────────────────────────────────────────────────────────────
    @GET("/health")
    suspend fun health(): Response<Map<String, String>>

    @GET("/api/v1/status")
    suspend fun getStatus(): Response<StatusDto>

    // ── Dashboard (Module 02) ──────────────────────────────────────────────
    @GET("/api/v1/dashboard")
    suspend fun getDashboard(): Response<DashboardDto>

    @GET("/api/v1/portfolio/metrics")
    suspend fun getPortfolioMetrics(): Response<PortfolioMetricsDto>

    // ── Agents (Module 03) ────────────────────────────────────────────────
    @GET("/api/v1/agents")
    suspend fun listAgents(): Response<List<AgentDto>>

    @GET("/api/v1/agents/{id}")
    suspend fun getAgent(@Path("id") id: String): Response<AgentDto>

    @POST("/api/v1/agents/{id}/control")
    suspend fun controlAgent(
        @Path("id") id: String,
        @Body control: ControlRequest,
    ): Response<Unit>

    // ── Vault (Module 04) ─────────────────────────────────────────────────
    @GET("/api/v1/vault")
    suspend fun getVault(): Response<List<VaultPositionDto>>

    @POST("/api/v1/vault/rebalance")
    suspend fun rebalanceVault(): Response<Map<String, String>>

    @POST("/api/v1/vault/withdraw")
    suspend fun withdrawProfits(): Response<Map<String, String>>

    // ── Strategies (Module 05) ────────────────────────────────────────────
    @GET("/api/v1/strategies")
    suspend fun listStrategies(): Response<List<StrategyDto>>

    @POST("/api/v1/strategies/mode")
    suspend fun updateStrategyMode(@Body body: StrategyModeRequest): Response<Map<String, String>>

    // ── Notifications (Module 06) ─────────────────────────────────────────
    @GET("/api/v1/notifications")
    suspend fun getNotifications(@Query("limit") limit: Int = 50): Response<List<NotificationDto>>

    @DELETE("/api/v1/notifications/{id}")
    suspend fun dismissNotification(@Path("id") id: String): Response<Map<String, String>>

    // ── Signals + Gas (Module 16) ─────────────────────────────────────────
    @GET("/api/v1/signals")
    suspend fun getSignals(): Response<List<SignalDto>>

    @GET("/api/v1/gas")
    suspend fun getGasEstimates(): Response<List<GasEstimateDto>>

    @POST("/api/v1/trade")
    suspend fun executeTrade(@Body body: TradeRequest): Response<Map<String, String>>

    // ── Emergency (Module 08) ─────────────────────────────────────────────
    @POST("/api/v1/emergency/safe_mode")
    suspend fun activateSafeMode(): Response<Map<String, String>>

    @POST("/api/v1/emergency/shutdown")
    suspend fun emergencyShutdown(): Response<Map<String, String>>

    @POST("/api/v1/emergency/resume")
    suspend fun resumeOperations(): Response<Map<String, String>>
}
