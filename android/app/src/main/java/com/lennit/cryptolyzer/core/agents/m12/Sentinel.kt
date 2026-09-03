package com.lennit.cryptolyzer.core.agents.m12

import com.lennit.cryptolyzer.core.model.AgentDescriptor
import com.lennit.cryptolyzer.core.model.AgentId
import com.lennit.cryptolyzer.core.model.AgentStatus
import com.lennit.cryptolyzer.core.model.RiskAssessment
import com.lennit.cryptolyzer.core.model.Opportunity
import com.lennit.cryptolyzer.core.agents.Agent

interface RiskRule {
    fun evaluate(opportunity: Opportunity): RiskFinding?
}

data class RiskFinding(val code: String, val severity: Double, val reason: String) {
    init { require(severity in 0.0..1.0) }
}

class SentinelAgent(private val rules: List<RiskRule>) : Agent {
    override val descriptor = AgentDescriptor(AgentId("M12"), "Sentinelyzer", AgentStatus.Ready)
    override suspend fun start() = Unit
    override suspend fun stop() = Unit

    fun assess(opportunity: Opportunity): RiskAssessment {
        val findings = rules.mapNotNull { it.evaluate(opportunity) }
        val score = findings.maxOfOrNull { it.severity } ?: opportunity.riskScore
        return RiskAssessment(
            opportunityId = opportunity.id,
            score = score.coerceIn(0.0, 1.0),
            approved = findings.none { it.severity >= 0.35 },
            reasons = findings.map { "${it.code}: ${it.reason}" }
        )
    }
}
