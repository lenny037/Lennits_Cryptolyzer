package com.lennit.cryptolyzer.core.security

import com.lennit.cryptolyzer.core.model.ExecutionDecision
import com.lennit.cryptolyzer.core.model.ExecutionRequest
import com.lennit.cryptolyzer.core.model.RiskAssessment
import com.lennit.cryptolyzer.core.model.SystemMode

class ExecutionPolicy {
    fun evaluate(
        mode: SystemMode,
        request: ExecutionRequest,
        risk: RiskAssessment,
        simulationPassed: Boolean
    ): ExecutionDecision {
        if (mode == SystemMode.SAFE_MODE || mode == SystemMode.SHUTDOWN) return ExecutionDecision.DENY
        if (request.simulationRequired && !simulationPassed) return ExecutionDecision.SIMULATE_ONLY
        if (!risk.approved || risk.score > 0.35) return ExecutionDecision.DENY
        return when (mode) {
            SystemMode.OBSERVE, SystemMode.PAPER, SystemMode.SHADOW, SystemMode.SIMULATE -> ExecutionDecision.SIMULATE_ONLY
            SystemMode.MANUAL_APPROVAL -> ExecutionDecision.REQUIRE_APPROVAL
            SystemMode.CONTROLLED_LIVE -> ExecutionDecision.APPROVE
            SystemMode.SAFE_MODE, SystemMode.SHUTDOWN -> ExecutionDecision.DENY
        }
    }
}
