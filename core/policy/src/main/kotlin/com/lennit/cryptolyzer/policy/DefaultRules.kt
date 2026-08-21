package com.lennit.cryptolyzer.policy

/** Observation and simulation never move value, so they are cleared immediately. */
public object ReadOnlyIntentRule : PolicyRule {
    override val id: String = "intent.read-only"

    override fun evaluate(
        request: ExecutionRequest,
        context: PolicyContext,
        limits: PolicyLimits,
    ): PolicyDecision? = when (request.intent) {
        ExecutionIntent.Observe, ExecutionIntent.Simulate ->
            PolicyDecision.Allow(id, "Intent ${request.intent} moves no value")
        ExecutionIntent.Transact -> null
    }
}

/** Safe mode is absolute: nothing that moves value proceeds while it is latched. */
public object SafeModeRule : PolicyRule {
    override val id: String = "posture.safe-mode"

    override fun evaluate(
        request: ExecutionRequest,
        context: PolicyContext,
        limits: PolicyLimits,
    ): PolicyDecision? = if (context.safeMode) {
        PolicyDecision.Deny(id, "Safe mode is active; value-moving actions are suspended")
    } else {
        null
    }
}

/** Above the approval threshold a human must have said yes, explicitly, for this request. */
public object HumanApprovalRule : PolicyRule {
    override val id: String = "approval.human-required"

    override fun evaluate(
        request: ExecutionRequest,
        context: PolicyContext,
        limits: PolicyLimits,
    ): PolicyDecision? {
        val overThreshold = request.notional.amount > limits.humanApprovalThreshold
        val required = request.requiresHumanApproval || overThreshold
        return if (required && !request.humanApprovalGranted) {
            PolicyDecision.Deny(
                id,
                "Human approval required for notional ${request.notional} and was not granted",
            )
        } else {
            null
        }
    }
}

/** A transaction with no passing simulation is refused. Unavailable is not a pass. */
public object SimulationRequiredRule : PolicyRule {
    override val id: String = "simulation.required"

    override fun evaluate(
        request: ExecutionRequest,
        context: PolicyContext,
        limits: PolicyLimits,
    ): PolicyDecision? {
        val simulation = request.simulation
            ?: return PolicyDecision.SimulateOnly(id, "No simulation attached; simulate before transacting")
        return when (simulation.status) {
            SimulationResult.Status.Passed -> null
            SimulationResult.Status.Failed ->
                PolicyDecision.Deny(id, "Simulation failed: ${simulation.detail ?: "no detail"}")
            SimulationResult.Status.Unavailable ->
                PolicyDecision.SimulateOnly(id, "Simulation unavailable; treated as not simulated")
        }
    }
}

/** A stale simulation describes a state of the world that no longer exists. */
public object SimulationFreshnessRule : PolicyRule {
    override val id: String = "simulation.freshness"

    override fun evaluate(
        request: ExecutionRequest,
        context: PolicyContext,
        limits: PolicyLimits,
    ): PolicyDecision? {
        val simulation = request.simulation ?: return null
        val age = context.evaluatedAtEpochMillis - simulation.performedAtEpochMillis
        return when {
            age < 0 -> PolicyDecision.Deny(id, "Simulation timestamp is in the future; clock integrity failure")
            age > limits.maxSimulationAgeMillis ->
                PolicyDecision.SimulateOnly(id, "Simulation is ${age}ms old; re-simulate before transacting")
            else -> null
        }
    }
}

public object NotionalLimitRule : PolicyRule {
    override val id: String = "limit.notional"

    override fun evaluate(
        request: ExecutionRequest,
        context: PolicyContext,
        limits: PolicyLimits,
    ): PolicyDecision? = when {
        !request.notional.amount.isPositive ->
            PolicyDecision.Deny(id, "Notional must be positive")
        request.notional.amount > limits.maxNotionalPerAction ->
            PolicyDecision.Deny(
                id,
                "Notional ${request.notional} exceeds per-action limit " +
                    limits.maxNotionalPerAction.toPlainString(),
            )
        else -> null
    }
}

public object DailySpendRule : PolicyRule {
    override val id: String = "limit.daily-spend"

    override fun evaluate(
        request: ExecutionRequest,
        context: PolicyContext,
        limits: PolicyLimits,
    ): PolicyDecision? {
        val projected = context.dailySpendUsed + request.notional.amount
        return if (projected > limits.maxDailySpend) {
            PolicyDecision.Deny(
                id,
                "Projected daily spend ${projected.toPlainString()} exceeds cap " +
                    limits.maxDailySpend.toPlainString(),
            )
        } else {
            null
        }
    }
}

/** Capital must be observed as available. Optimism about balances is how overdrafts happen. */
public object TreasuryCoverageRule : PolicyRule {
    override val id: String = "treasury.coverage"

    override fun evaluate(
        request: ExecutionRequest,
        context: PolicyContext,
        limits: PolicyLimits,
    ): PolicyDecision? = if (request.notional.amount > context.treasuryAvailable) {
        PolicyDecision.Deny(
            id,
            "Notional ${request.notional} exceeds observed available capital " +
                context.treasuryAvailable.toPlainString(),
        )
    } else {
        null
    }
}

public object ExpectedValueRule : PolicyRule {
    override val id: String = "economics.expected-value"

    override fun evaluate(
        request: ExecutionRequest,
        context: PolicyContext,
        limits: PolicyLimits,
    ): PolicyDecision? = if (request.netExpected < limits.minNetExpected) {
        PolicyDecision.Deny(
            id,
            "Net expected ${request.netExpected.toPlainString()} is below floor " +
                limits.minNetExpected.toPlainString(),
        )
    } else {
        null
    }
}

public object ConfidenceRule : PolicyRule {
    override val id: String = "economics.confidence"

    override fun evaluate(
        request: ExecutionRequest,
        context: PolicyContext,
        limits: PolicyLimits,
    ): PolicyDecision? = if (request.confidence < limits.minConfidence) {
        PolicyDecision.SimulateOnly(
            id,
            "Confidence ${request.confidence} is below required ${limits.minConfidence}",
        )
    } else {
        null
    }
}

/**
 * Terminal rule. Reaching it means every control above was satisfied, and it exists so the
 * allow path is as explicit and as auditable as every refusal.
 */
public object FinalAllowRule : PolicyRule {
    override val id: String = "engine.all-controls-passed"

    override fun evaluate(
        request: ExecutionRequest,
        context: PolicyContext,
        limits: PolicyLimits,
    ): PolicyDecision = PolicyDecision.Allow(id, "All controls passed for ${request.intent}")
}
