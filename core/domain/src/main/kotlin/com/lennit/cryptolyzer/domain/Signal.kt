package com.lennit.cryptolyzer.domain

import com.lennit.cryptolyzer.contracts.ModuleId

/**
 * Canonical intelligence unit.
 *
 * Every observation, whether it came from a chain, a market feed, a social source or a sports
 * odds provider, is normalized into a [Signal] before any consumer sees it. That is what makes
 * the betting analytics a consumer of the same intelligence fabric rather than a second
 * application bolted alongside.
 */
public data class Signal(
    val id: String,
    val kind: SignalKind,
    val subject: String,
    val producedBy: ModuleId,
    val observedAtEpochMillis: Long,
    val quality: SignalQuality,
    val attributes: Map<String, String> = emptyMap(),
    val measures: Map<String, Amount> = emptyMap(),
) {
    init {
        require(id.isNotBlank()) { "Signal id cannot be blank" }
        require(subject.isNotBlank()) { "Signal subject cannot be blank" }
    }

    /**
     * Stable content fingerprint used for deduplication. Deliberately excludes [id] and the
     * observation timestamp so that the same fact arriving twice from two providers collapses
     * to one signal.
     */
    public val dedupeKey: String
        get() = buildString {
            append(kind.name).append('|').append(subject).append('|')
            attributes.toSortedMap().forEach { (k, v) -> append(k).append('=').append(v).append(';') }
            measures.toSortedMap().forEach { (k, v) -> append(k).append('=').append(v.toPlainString()).append(';') }
        }
}

public enum class SignalKind {
    ChainState,
    TokenTransfer,
    LiquidityChange,
    PriceObservation,
    GasObservation,
    RewardOpportunity,
    GovernanceEvent,
    SocialSentiment,
    SportsOdds,
    SystemTelemetry,
}

/**
 * How much a consumer may lean on a signal.
 *
 * [confidence] is the producer's own assessment; [freshnessMillis] and [sourceCount] let a
 * consumer apply its own discount. Anything that cannot state a provenance is [PROVISIONAL].
 */
public data class SignalQuality(
    val confidence: Confidence,
    val sourceCount: Int,
    val freshnessMillis: Long,
    val verified: Boolean,
) {
    init {
        require(sourceCount >= 0) { "sourceCount cannot be negative" }
        require(freshnessMillis >= 0) { "freshnessMillis cannot be negative" }
    }

    public companion object {
        public val PROVISIONAL: SignalQuality = SignalQuality(
            confidence = Confidence.of(0.2).getOrNull() ?: Confidence.ZERO,
            sourceCount = 1,
            freshnessMillis = 0,
            verified = false,
        )
    }
}
