package com.lennit.cryptolyzer.core.security

import com.lennit.cryptolyzer.core.model.ExecutionDecision
import com.lennit.cryptolyzer.core.model.ExecutionRequest
import com.lennit.cryptolyzer.core.model.RiskAssessment
import com.lennit.cryptolyzer.core.model.SystemMode

/** Central fail-closed decision boundary. It never signs or broadcasts transactions. */
class ExecutionPolicy(private val maximumApprovedRisk: Double = 0.35) {
    init { require(maximumApprovedRisk.isFinite() && maximumApprovedRisk in 0.0..1.0) }

    fun evaluate(
        mode: SystemMode,
        request: ExecutionRequest,
        risk: RiskAssessment,
        simulationPassed: Boolean
    ): ExecutionDecision {
        if (mode == SystemMode.SAFE_MODE || mode == SystemMode.SHUTDOWN) return ExecutionDecision.DENY
        if (!simulationPassed) return if (request.simulationRequired) ExecutionDecision.SIMULATE_ONLY else ExecutionDecision.DENY
        if (!risk.approved || !risk.score.isFinite() || risk.score !in 0.0..maximumApprovedRisk) {
            return ExecutionDecision.DENY
        }

        return when (mode) {
            SystemMode.OBSERVE,
            SystemMode.PAPER,
            SystemMode.SHADOW,
            SystemMode.SIMULATE -> ExecutionDecision.SIMULATE_ONLY
            SystemMode.MANUAL_APPROVAL -> ExecutionDecision.REQUIRE_APPROVAL
            SystemMode.CONTROLLED_LIVE -> ExecutionDecision.APPROVE
            SystemMode.SAFE_MODE,
            SystemMode.SHUTDOWN -> ExecutionDecision.DENY
        }
    }
}
