package com.lennit.cryptolyzer.core.security

import com.lennit.cryptolyzer.core.model.ExecutionDecision
import com.lennit.cryptolyzer.core.model.ExecutionRequest
import com.lennit.cryptolyzer.core.model.OpportunityId
import com.lennit.cryptolyzer.core.model.RiskAssessment
import com.lennit.cryptolyzer.core.model.SystemMode
import com.lennit.cryptolyzer.core.model.TransactionId
import org.junit.Assert.assertEquals
import org.junit.Test

class ExecutionPolicyTest {
    private val policy = ExecutionPolicy()
    private val request = ExecutionRequest(TransactionId("t1"), 1, "0x0", "0", "0x", true)
    private val safeRisk = RiskAssessment(OpportunityId("o1"), 0.1, true, emptyList())

    @Test fun safeModeAlwaysDenies() {
        assertEquals(ExecutionDecision.DENY, policy.evaluate(SystemMode.SAFE_MODE, request, safeRisk, true))
    }

    @Test fun failedSimulationCannotExecute() {
        assertEquals(ExecutionDecision.SIMULATE_ONLY, policy.evaluate(SystemMode.CONTROLLED_LIVE, request, safeRisk, false))
    }

    @Test fun failedOptionalSimulationStillDeniesLiveExecution() {
        val optional = request.copy(simulationRequired = false)
        assertEquals(ExecutionDecision.DENY, policy.evaluate(SystemMode.CONTROLLED_LIVE, optional, safeRisk, false))
    }

    @Test fun manualApprovalRequiresHumanDecision() {
        assertEquals(ExecutionDecision.REQUIRE_APPROVAL, policy.evaluate(SystemMode.MANUAL_APPROVAL, request, safeRisk, true))
    }

    @Test fun excessiveRiskDenies() {
        val risky = safeRisk.copy(score = 0.9)
        assertEquals(ExecutionDecision.DENY, policy.evaluate(SystemMode.CONTROLLED_LIVE, request, risky, true))
    }

    @Test fun nonFiniteRiskDenies() {
        val invalid = safeRisk.copy(score = Double.NaN)
        assertEquals(ExecutionDecision.DENY, policy.evaluate(SystemMode.CONTROLLED_LIVE, request, invalid, true))
    }
}
