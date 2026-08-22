package com.lennit.cryptolyzer.policy

import com.lennit.cryptolyzer.contracts.ModuleId
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.domain.Amount
import com.lennit.cryptolyzer.domain.Asset
import com.lennit.cryptolyzer.domain.AssetAmount
import com.lennit.cryptolyzer.domain.ChainId
import com.lennit.cryptolyzer.domain.Confidence

internal fun <T> Outcome<T>.expect(): T = when (this) {
    is Outcome.Success -> value
    is Outcome.Failure -> throw AssertionError("expected success but got: ${error.message}")
}

internal fun amount(text: String): Amount = Amount.parse(text).expect()

internal fun confidence(value: Double): Confidence = Confidence.of(value).expect()

internal val usdc: Asset = Asset(chainId = ChainId.BASE, symbol = "USDC", decimals = 6)

internal val defaultLimits: PolicyLimits = PolicyLimits(
    maxNotionalPerAction = amount("100"),
    maxDailySpend = amount("250"),
    minNetExpected = amount("1"),
    minConfidence = confidence(0.6),
    maxSimulationAgeMillis = 30_000,
    humanApprovalThreshold = amount("50"),
)

internal val cleanContext: PolicyContext = PolicyContext(
    safeMode = false,
    treasuryAvailable = amount("1000"),
    dailySpendUsed = Amount.ZERO,
    evaluatedAtEpochMillis = 100_000,
)

internal fun passedSimulation(atEpochMillis: Long = 95_000): SimulationResult = SimulationResult(
    status = SimulationResult.Status.Passed,
    expectedGain = amount("10"),
    expectedCost = amount("2"),
    performedAtEpochMillis = atEpochMillis,
)

/** A request that satisfies every control, so each test can break exactly one thing. */
internal fun allowableRequest(
    intent: ExecutionIntent = ExecutionIntent.Transact,
    notional: String = "10",
    expectedGain: String = "12",
    estimatedCost: String = "2",
    simulation: SimulationResult? = passedSimulation(),
    confidenceValue: Double = 0.8,
    requiresHumanApproval: Boolean = false,
    humanApprovalGranted: Boolean = false,
): ExecutionRequest = ExecutionRequest(
    requestId = "req-1",
    requestedBy = ModuleId.DefiStrategy,
    intent = intent,
    notional = AssetAmount(usdc, amount(notional)),
    expectedGain = amount(expectedGain),
    estimatedCost = amount(estimatedCost),
    simulation = simulation,
    confidence = confidence(confidenceValue),
    requiresHumanApproval = requiresHumanApproval,
    humanApprovalGranted = humanApprovalGranted,
    requestedAtEpochMillis = 99_000,
)
