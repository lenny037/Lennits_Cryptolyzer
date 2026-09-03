package com.lennit.cryptolyzer.core.model

@JvmInline value class AgentId(val value: String)
@JvmInline value class OpportunityId(val value: String)
@JvmInline value class TransactionId(val value: String)

sealed interface AgentStatus {
    data object Disabled : AgentStatus
    data object Initializing : AgentStatus
    data object Ready : AgentStatus
    data class Failed(val reason: String) : AgentStatus
}

data class AgentDescriptor(
    val id: AgentId,
    val name: String,
    val status: AgentStatus = AgentStatus.Disabled
)

data class MarketSignal(
    val source: String,
    val symbol: String,
    val observedAtEpochMs: Long,
    val confidence: Double,
    val payloadJson: String = "{}"
) {
    init {
        require(confidence in 0.0..1.0) { "confidence must be between 0 and 1" }
    }
}

data class Opportunity(
    val id: OpportunityId,
    val strategy: String,
    val expectedValue: Double,
    val riskScore: Double,
    val createdAtEpochMs: Long
) {
    init {
        require(riskScore in 0.0..1.0) { "riskScore must be between 0 and 1" }
    }
}

data class RiskAssessment(
    val opportunityId: OpportunityId,
    val score: Double,
    val approved: Boolean,
    val reasons: List<String>
) {
    init { require(score in 0.0..1.0) { "score must be between 0 and 1" } }
}

data class ExecutionRequest(
    val id: TransactionId,
    val chainId: Long,
    val to: String,
    val valueWei: String,
    val dataHex: String,
    val simulationRequired: Boolean = true
)

enum class ExecutionDecision { DENY, SIMULATE_ONLY, REQUIRE_APPROVAL, APPROVE }

data class ExecutionResult(
    val requestId: TransactionId,
    val decision: ExecutionDecision,
    val transactionHash: String? = null,
    val reason: String? = null
)

data class AuditEvent(
    val type: String,
    val actor: String,
    val timestampEpochMs: Long,
    val detailsJson: String = "{}"
)
