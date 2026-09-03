package com.lennit.cryptolyzer.core.agents

import com.lennit.cryptolyzer.core.agents.m12.RiskFinding
import com.lennit.cryptolyzer.core.agents.m12.RiskRule
import com.lennit.cryptolyzer.core.agents.m12.SentinelAgent
import com.lennit.cryptolyzer.core.model.Opportunity
import com.lennit.cryptolyzer.core.model.OpportunityId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Test

class SentinelTest {
    @Test fun highSeverityFindingDeniesOpportunity() {
        val rule = object : RiskRule {
            override fun evaluate(opportunity: Opportunity) = RiskFinding("TEST_HIGH", 0.9, "test threat")
        }
        val result = SentinelAgent(listOf(rule)).assess(
            Opportunity(OpportunityId("o1"), "test", 1.0, 0.1, 0L)
        )
        assertFalse(result.approved)
        assertEquals(0.9, result.score, 0.0001)
    }
}
