package com.lennit.cryptolyzer.core.orchestration

import com.lennit.cryptolyzer.core.agents.m00.ExtractorAgent
import com.lennit.cryptolyzer.core.agents.m08.BlockchainGateway
import com.lennit.cryptolyzer.core.agents.m12.SentinelAgent
import com.lennit.cryptolyzer.core.agents.m16.CortexolyzerAgent
import com.lennit.cryptolyzer.core.model.ExecutionDecision
import com.lennit.cryptolyzer.core.model.ExecutionRequest
import com.lennit.cryptolyzer.core.model.Opportunity
import com.lennit.cryptolyzer.core.model.OpportunityId
import com.lennit.cryptolyzer.core.model.SystemMode
import com.lennit.cryptolyzer.core.security.ExecutionPolicy

/** Coordinates analysis only. It never owns keys or signs transactions. */
class Orchestralyzer(
    private val extractor: ExtractorAgent,
    private val chainsighter: BlockchainGateway,
    private val sentinel: SentinelAgent,
    private val cortex: CortexolyzerAgent,
    private val policy: ExecutionPolicy
) {
    suspend fun evaluate(request: ExecutionRequest, mode: SystemMode): ExecutionDecision {
        val simulation = chainsighter.simulateCall(request.to, request.dataHex, request.valueWei)
        if (!simulation.success) return ExecutionDecision.DENY

        val signals = extractor.extract()
        cortex.remember("orchestration", request.id.value, "{\"signals\":${signals.size}}")

        val opportunity = Opportunity(
            id = OpportunityId(request.id.value),
            strategy = "runtime-evaluation",
            expectedValue = 0.0,
            riskScore = 0.0,
            createdAtEpochMs = System.currentTimeMillis()
        )
        val risk = sentinel.assess(opportunity)
        return policy.evaluate(mode, request, risk, simulationPassed = true)
    }
}
