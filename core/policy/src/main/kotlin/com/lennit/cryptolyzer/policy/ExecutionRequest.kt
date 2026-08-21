package com.lennit.cryptolyzer.policy

import com.lennit.cryptolyzer.contracts.ModuleId
import com.lennit.cryptolyzer.domain.Amount
import com.lennit.cryptolyzer.domain.AssetAmount
import com.lennit.cryptolyzer.domain.Confidence

/**
 * Everything a policy needs to judge a consequential action.
 *
 * Strategy modules never call an executor directly. They submit a request, and the only thing
 * they receive back is a [PolicyDecision]. That indirection is the whole point of Phase 8: there
 * is no code path from a strategy to a signer.
 */
public data class ExecutionRequest(
    val requestId: String,
    val requestedBy: ModuleId,
    val intent: ExecutionIntent,
    val notional: AssetAmount,
    val expectedGain: Amount,
    val estimatedCost: Amount,
    val simulation: SimulationResult?,
    val confidence: Confidence,
    val requiresHumanApproval: Boolean,
    val humanApprovalGranted: Boolean = false,
    val requestedAtEpochMillis: Long,
) {
    public val netExpected: Amount get() = expectedGain - estimatedCost
}

public enum class ExecutionIntent {
    /** Read-only. No signing, no funds movement. */
    Observe,

    /** Off-chain or eth_call simulation only. */
    Simulate,

    /** Moves value. The only intent that can ever require a signature. */
    Transact,
}

/**
 * Result of simulating an action before committing to it.
 *
 * [status] is a tri-state on purpose. A simulation that could not be performed is *not* a passing
 * simulation, and the fail-closed rules treat Unavailable exactly like a failure.
 */
public data class SimulationResult(
    val status: Status,
    val expectedGain: Amount,
    val expectedCost: Amount,
    val performedAtEpochMillis: Long,
    val detail: String? = null,
) {
    public enum class Status { Passed, Failed, Unavailable }

    public val net: Amount get() = expectedGain - expectedCost
}

/** Runtime posture. [SafeMode] is a one-way latch until an operator clears it. */
public data class PolicyContext(
    val safeMode: Boolean,
    val treasuryAvailable: Amount,
    val dailySpendUsed: Amount,
    val evaluatedAtEpochMillis: Long,
    val networkAvailable: Boolean = true,
)

/** Static limits. Kept as data so they can be configured, audited and diffed. */
public data class PolicyLimits(
    val maxNotionalPerAction: Amount,
    val maxDailySpend: Amount,
    val minNetExpected: Amount,
    val minConfidence: Confidence,
    val maxSimulationAgeMillis: Long,
    val humanApprovalThreshold: Amount,
)
