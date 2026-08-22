package com.lennit.cryptolyzer.policy

import com.lennit.cryptolyzer.contracts.Clock
import com.lennit.cryptolyzer.contracts.ModuleId

/** The only three answers a policy is allowed to give. There is no implicit fourth. */
public sealed interface PolicyDecision {

    public val ruleId: String

    /** Cleared to proceed with the requested intent. */
    public data class Allow(override val ruleId: String, val reason: String) : PolicyDecision

    /** Downgraded: the action may be simulated but not committed. */
    public data class SimulateOnly(override val ruleId: String, val reason: String) : PolicyDecision

    /** Refused. Carries the exact rule so refusals are explainable to the user and to an auditor. */
    public data class Deny(override val ruleId: String, val reason: String) : PolicyDecision
}

/** A single, independently testable control. Rules never mutate state. */
public fun interface PolicyRule {
    public val id: String get() = "unnamed"

    public fun evaluate(request: ExecutionRequest, context: PolicyContext, limits: PolicyLimits): PolicyDecision?
}

/**
 * Deterministic, fail-closed policy evaluation.
 *
 * Two properties matter more than the individual rules:
 *  1. **Fail closed.** The default answer is Deny. A rule set that returns nothing, a rule that
 *     throws, an unclassifiable state: all resolve to Deny, never to Allow.
 *  2. **Ordered and total.** Rules are evaluated in declaration order and the first non-null
 *     decision wins, so the outcome for a given input is a pure function and can be pinned by a
 *     decision table test.
 *
 * Every evaluation produces an [AuditRecord] whether it allowed or refused. An unlogged refusal
 * is indistinguishable from a bug.
 */
public class PolicyEngine(
    private val limits: PolicyLimits,
    private val clock: Clock,
    rules: List<PolicyRule> = defaultRules(),
) {
    private val rules: List<PolicyRule> = rules.toList()

    public fun evaluate(request: ExecutionRequest, context: PolicyContext): PolicyEvaluation {
        val decision = try {
            rules.firstNotNullOfOrNull { rule -> rule.evaluate(request, context, limits) }
                ?: PolicyDecision.Deny(
                    ruleId = "engine.no-rule-matched",
                    reason = "No rule produced a decision; denying by default",
                )
        } catch (throwable: Throwable) {
            if (throwable is kotlinx.coroutines.CancellationException) throw throwable
            PolicyDecision.Deny(
                ruleId = "engine.rule-threw",
                reason = "Rule evaluation threw ${throwable::class.simpleName}; denying by default",
            )
        }
        return PolicyEvaluation(
            decision = decision,
            audit = AuditRecord(
                requestId = request.requestId,
                requestedBy = request.requestedBy,
                intent = request.intent,
                decisionKind = decision::class.simpleName ?: "Unknown",
                ruleId = decision.ruleId,
                reason = when (decision) {
                    is PolicyDecision.Allow -> decision.reason
                    is PolicyDecision.SimulateOnly -> decision.reason
                    is PolicyDecision.Deny -> decision.reason
                },
                notional = request.notional.toString(),
                decidedAtEpochMillis = clock.nowEpochMillis(),
            ),
        )
    }

    public companion object {

        /**
         * The baseline control set. Order is deliberate: cheapest and most absolute controls
         * first, so an unsafe request is refused before any expensive consideration.
         */
        public fun defaultRules(): List<PolicyRule> = listOf(
            ReadOnlyIntentRule,
            SafeModeRule,
            HumanApprovalRule,
            SimulationRequiredRule,
            SimulationFreshnessRule,
            NotionalLimitRule,
            DailySpendRule,
            TreasuryCoverageRule,
            ExpectedValueRule,
            ConfidenceRule,
            FinalAllowRule,
        )
    }
}

public data class PolicyEvaluation(val decision: PolicyDecision, val audit: AuditRecord)

/** Append-only decision record. Written to the event log, never updated in place. */
public data class AuditRecord(
    val requestId: String,
    val requestedBy: ModuleId,
    val intent: ExecutionIntent,
    val decisionKind: String,
    val ruleId: String,
    val reason: String,
    val notional: String,
    val decidedAtEpochMillis: Long,
) {
    public fun toEventPayload(): Map<String, String> = mapOf(
        "request_id" to requestId,
        "requested_by" to requestedBy.code,
        "intent" to intent.name,
        "decision" to decisionKind,
        "rule_id" to ruleId,
        "reason" to reason,
        "notional" to notional,
    )
}
