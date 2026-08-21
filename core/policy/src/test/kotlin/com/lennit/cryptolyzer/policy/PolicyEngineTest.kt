package com.lennit.cryptolyzer.policy

import com.lennit.cryptolyzer.contracts.MutableClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The policy decision table. Each case fixes exactly one deviation from an allowable request, so
 * a failure names the control that regressed.
 */
class PolicyEngineTest {

    private val clock = MutableClock(100_000)
    private val engine = PolicyEngine(defaultLimits, clock)

    private fun decide(
        request: ExecutionRequest,
        context: PolicyContext = cleanContext,
    ): PolicyDecision = engine.evaluate(request, context).decision

    // ---------------------------------------------------------------- allow paths

    @Test
    fun `an observation is cleared without any further consideration`() {
        val decision = decide(allowableRequest(intent = ExecutionIntent.Observe, simulation = null))
        assertTrue(decision is PolicyDecision.Allow)
        assertEquals("intent.read-only", decision.ruleId)
    }

    @Test
    fun `a simulation intent is cleared even in safe mode, because it moves no value`() {
        val decision = decide(
            allowableRequest(intent = ExecutionIntent.Simulate, simulation = null),
            context = cleanContext.copy(safeMode = true),
        )
        assertTrue(decision is PolicyDecision.Allow)
        assertEquals("intent.read-only", decision.ruleId)
    }

    @Test
    fun `a fully compliant transaction is allowed by the terminal rule`() {
        val decision = decide(allowableRequest())
        assertTrue(decision is PolicyDecision.Allow, decision.toString())
        assertEquals("engine.all-controls-passed", decision.ruleId)
    }

    // ---------------------------------------------------------------- deny paths

    @Test
    fun `safe mode denies anything that moves value`() {
        val decision = decide(allowableRequest(), cleanContext.copy(safeMode = true))
        assertTrue(decision is PolicyDecision.Deny)
        assertEquals("posture.safe-mode", decision.ruleId)
    }

    @Test
    fun `safe mode outranks every economic consideration`() {
        val decision = decide(
            allowableRequest(notional = "1000", expectedGain = "0"),
            cleanContext.copy(safeMode = true),
        )
        assertEquals("posture.safe-mode", decision.ruleId)
    }

    @Test
    fun `an amount over the approval threshold is denied without explicit human approval`() {
        val decision = decide(allowableRequest(notional = "50.01"))
        assertTrue(decision is PolicyDecision.Deny)
        assertEquals("approval.human-required", decision.ruleId)
    }

    @Test
    fun `granted human approval clears the threshold control`() {
        val decision = decide(allowableRequest(notional = "60", humanApprovalGranted = true))
        assertTrue(decision is PolicyDecision.Allow, decision.toString())
    }

    @Test
    fun `an explicit approval requirement applies below the threshold too`() {
        assertEquals(
            "approval.human-required",
            decide(allowableRequest(notional = "1", requiresHumanApproval = true)).ruleId,
        )
        assertTrue(
            decide(
                allowableRequest(notional = "1", requiresHumanApproval = true, humanApprovalGranted = true),
            ) is PolicyDecision.Allow,
        )
    }

    @Test
    fun `a failed simulation is denied`() {
        val decision = decide(
            allowableRequest(
                simulation = passedSimulation().copy(
                    status = SimulationResult.Status.Failed,
                    detail = "reverted: insufficient allowance",
                ),
            ),
        )
        assertTrue(decision is PolicyDecision.Deny)
        assertEquals("simulation.required", decision.ruleId)
        assertTrue((decision as PolicyDecision.Deny).reason.contains("allowance"))
    }

    @Test
    fun `a simulation timestamped in the future is a clock integrity failure`() {
        val decision = decide(allowableRequest(simulation = passedSimulation(atEpochMillis = 100_001)))
        assertTrue(decision is PolicyDecision.Deny)
        assertEquals("simulation.freshness", decision.ruleId)
    }

    @Test
    fun `a notional above the per-action limit is denied`() {
        val decision = decide(allowableRequest(notional = "100.01", humanApprovalGranted = true))
        assertTrue(decision is PolicyDecision.Deny)
        assertEquals("limit.notional", decision.ruleId)
    }

    @Test
    fun `a zero or negative notional is denied`() {
        assertEquals("limit.notional", decide(allowableRequest(notional = "0")).ruleId)
        assertEquals("limit.notional", decide(allowableRequest(notional = "-5")).ruleId)
    }

    @Test
    fun `the projected daily spend, not just this action, is what the cap applies to`() {
        val nearCap = cleanContext.copy(dailySpendUsed = amount("245"))
        val decision = decide(allowableRequest(notional = "10"), nearCap)
        assertTrue(decision is PolicyDecision.Deny)
        assertEquals("limit.daily-spend", decision.ruleId)
    }

    @Test
    fun `spending exactly up to the cap is permitted`() {
        val atCap = cleanContext.copy(dailySpendUsed = amount("240"))
        assertTrue(decide(allowableRequest(notional = "10"), atCap) is PolicyDecision.Allow)
    }

    @Test
    fun `capital that has not been observed as available cannot be committed`() {
        val poor = cleanContext.copy(treasuryAvailable = amount("9.99"))
        val decision = decide(allowableRequest(notional = "10"), poor)
        assertTrue(decision is PolicyDecision.Deny)
        assertEquals("treasury.coverage", decision.ruleId)
    }

    @Test
    fun `an action whose net expectation is below the floor is denied`() {
        val decision = decide(allowableRequest(expectedGain = "2.5", estimatedCost = "2"))
        assertTrue(decision is PolicyDecision.Deny)
        assertEquals("economics.expected-value", decision.ruleId)
    }

    @Test
    fun `cost is netted against gain rather than ignored`() {
        assertTrue(decide(allowableRequest(expectedGain = "100", estimatedCost = "99.5")) is PolicyDecision.Deny)
        assertTrue(decide(allowableRequest(expectedGain = "100", estimatedCost = "99")) is PolicyDecision.Allow)
    }

    // ---------------------------------------------------------------- downgrade paths

    @Test
    fun `a transaction with no simulation is downgraded to simulate-only`() {
        val decision = decide(allowableRequest(simulation = null))
        assertTrue(decision is PolicyDecision.SimulateOnly)
        assertEquals("simulation.required", decision.ruleId)
    }

    @Test
    fun `an unavailable simulation is treated as no simulation, never as a pass`() {
        val decision = decide(
            allowableRequest(simulation = passedSimulation().copy(status = SimulationResult.Status.Unavailable)),
        )
        assertTrue(decision is PolicyDecision.SimulateOnly)
        assertEquals("simulation.required", decision.ruleId)
    }

    @Test
    fun `a stale simulation is downgraded, because it describes a world that no longer exists`() {
        val decision = decide(allowableRequest(simulation = passedSimulation(atEpochMillis = 69_999)))
        assertTrue(decision is PolicyDecision.SimulateOnly)
        assertEquals("simulation.freshness", decision.ruleId)
    }

    @Test
    fun `a simulation at exactly the age limit is still fresh`() {
        assertTrue(decide(allowableRequest(simulation = passedSimulation(atEpochMillis = 70_000))) is PolicyDecision.Allow)
    }

    @Test
    fun `low confidence is downgraded rather than denied outright`() {
        val decision = decide(allowableRequest(confidenceValue = 0.59))
        assertTrue(decision is PolicyDecision.SimulateOnly)
        assertEquals("economics.confidence", decision.ruleId)
    }

    @Test
    fun `confidence exactly at the minimum is accepted`() {
        assertTrue(decide(allowableRequest(confidenceValue = 0.6)) is PolicyDecision.Allow)
    }

    // ---------------------------------------------------------------- fail-closed behaviour

    @Test
    fun `an empty rule set denies rather than allowing by omission`() {
        val decision = PolicyEngine(defaultLimits, clock, rules = emptyList())
            .evaluate(allowableRequest(), cleanContext).decision
        assertTrue(decision is PolicyDecision.Deny)
        assertEquals("engine.no-rule-matched", decision.ruleId)
    }

    @Test
    fun `a rule set where no rule matches denies`() {
        val abstaining = PolicyRule { _, _, _ -> null }
        val decision = PolicyEngine(defaultLimits, clock, rules = listOf(abstaining))
            .evaluate(allowableRequest(), cleanContext).decision
        assertEquals("engine.no-rule-matched", decision.ruleId)
    }

    @Test
    fun `a rule that throws denies instead of propagating the defect as an allow`() {
        val broken = PolicyRule { _, _, _ -> error("rule bug") }
        val decision = PolicyEngine(defaultLimits, clock, rules = listOf(broken))
            .evaluate(allowableRequest(), cleanContext).decision
        assertTrue(decision is PolicyDecision.Deny)
        assertEquals("engine.rule-threw", decision.ruleId)
    }

    @Test
    fun `a rule that throws after an earlier rule already decided does not affect the decision`() {
        val allowing = PolicyRule { _, _, _ -> PolicyDecision.Allow("test.allow", "ok") }
        val broken = PolicyRule { _, _, _ -> error("never reached") }
        val decision = PolicyEngine(defaultLimits, clock, rules = listOf(allowing, broken))
            .evaluate(allowableRequest(), cleanContext).decision
        assertEquals("test.allow", decision.ruleId)
    }

    @Test
    fun `rule order is honoured, so the first matching control wins`() {
        val first = PolicyRule { _, _, _ -> PolicyDecision.Deny("first", "first") }
        val second = PolicyRule { _, _, _ -> PolicyDecision.Allow("second", "second") }
        assertEquals(
            "first",
            PolicyEngine(defaultLimits, clock, rules = listOf(first, second))
                .evaluate(allowableRequest(), cleanContext).decision.ruleId,
        )
    }

    @Test
    fun `evaluation is a pure function of its inputs`() {
        val request = allowableRequest()
        val decisions = (1..25).map { decide(request) }
        assertEquals(1, decisions.toSet().size, "the same input produced different decisions: $decisions")
    }

    // ---------------------------------------------------------------- audit

    @Test
    fun `every decision produces an audit record, refusals included`() {
        listOf(
            allowableRequest(),
            allowableRequest(notional = "1000"),
            allowableRequest(simulation = null),
        ).forEach { request ->
            val evaluation = engine.evaluate(request, cleanContext)
            assertEquals(request.requestId, evaluation.audit.requestId)
            assertEquals(evaluation.decision.ruleId, evaluation.audit.ruleId)
            assertTrue(evaluation.audit.reason.isNotBlank())
            assertEquals(100_000, evaluation.audit.decidedAtEpochMillis)
        }
    }

    @Test
    fun `the audit record names the deciding module and intent for attribution`() {
        val audit = engine.evaluate(allowableRequest(), cleanContext).audit
        val payload = audit.toEventPayload()
        assertEquals("M07", payload["requested_by"])
        assertEquals("Transact", payload["intent"])
        assertEquals("Allow", payload["decision"])
        assertEquals("engine.all-controls-passed", payload["rule_id"])
    }

    @Test
    fun `the audit clock is the injected one, so records are reproducible in tests`() {
        clock.advanceBy(5_000)
        assertEquals(105_000, engine.evaluate(allowableRequest(), cleanContext).audit.decidedAtEpochMillis)
    }
}
