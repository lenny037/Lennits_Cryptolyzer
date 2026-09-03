package com.lennit.cryptolyzer.core.modules

/** Module boundaries derived from the project specification. Implementations are added only after their contracts are validated. */
object FinancialModuleIds {
    const val M02_TREASURY = "M02"
    const val M03_AIRDROP = "M03"
    const val M04_REWARDS = "M04"
    const val M05_PREDICTION = "M05"
    const val M06_MEV = "M06"
    const val M07_DEFI = "M07"
    const val M09_SOCIAL = "M09"
    const val M10_TOKENOMICS = "M10"
    const val M11_GOVERNANCE = "M11"
    const val M13_IDENTITY = "M13"
    const val M15_ANALYTICS = "M15"
    const val M17_AI_BRIDGE = "M17"
    const val M18_MOBILE = "M18"
    const val M19_MONETIZATION = "M19"
    const val M20_LEARNING = "M20"
}

data class TreasurySnapshot(
    val totalValueUsd: Double,
    val availableValueUsd: Double,
    val reservedValueUsd: Double,
    val asOfEpochMs: Long
)

data class Prediction(
    val subject: String,
    val probability: Double,
    val horizonSeconds: Long,
    val modelVersion: String
) {
    init { require(probability in 0.0..1.0) }
}

data class SocialSignal(
    val platform: String,
    val subject: String,
    val sentiment: Double,
    val observedAtEpochMs: Long
) {
    init { require(sentiment in -1.0..1.0) }
}
