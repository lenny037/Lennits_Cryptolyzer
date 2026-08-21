package com.lennit.cryptolyzer.domain

import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import java.math.BigDecimal

/** A probability in [0, 1], exact and validated. */
public class Confidence private constructor(public val value: BigDecimal) : Comparable<Confidence> {

    public fun asPercent(): BigDecimal = value.movePointRight(2)

    override fun compareTo(other: Confidence): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean =
        this === other || (other is Confidence && value.compareTo(other.value) == 0)

    override fun hashCode(): Int = value.stripTrailingZeros().hashCode()

    override fun toString(): String = "Confidence(${value.toPlainString()})"

    public companion object {
        public val ZERO: Confidence = Confidence(BigDecimal.ZERO)
        public val ONE: Confidence = Confidence(BigDecimal.ONE)

        public fun of(value: BigDecimal): Outcome<Confidence> =
            if (value.signum() < 0 || value > BigDecimal.ONE) {
                Outcome.failure(PlatformError.Validation("Confidence must be within [0,1], got $value"))
            } else {
                Outcome.success(Confidence(value))
            }

        /**
         * Convenience boundary conversion for probabilities produced by model code, which is
         * natively Double. A probability is not a monetary value, so the precision concern that
         * bans Double for money does not apply; the value is widened to BigDecimal immediately and
         * validated, and no arithmetic is ever performed on the Double itself.
         */
        public fun of(value: Double): Outcome<Confidence> = // float-guard:allow probability, not money (ADR-0003)
            of(BigDecimal.valueOf(value))
    }
}

/**
 * Output of a model, always tagged with the exact model version that produced it.
 *
 * Untagged predictions cannot be evaluated after the fact, which is what makes a learning loop
 * (Phase 13) meaningless. The evaluation record is a separate, immutable fact appended later.
 */
public data class Prediction(
    val id: String,
    val modelVersion: ModelVersion,
    val subject: String,
    val horizonMillis: Long,
    val expectedValue: Amount,
    val confidence: Confidence,
    val createdAtEpochMillis: Long,
    val featureFingerprint: String,
) {
    init {
        require(horizonMillis > 0) { "Prediction horizon must be positive" }
        require(featureFingerprint.isNotBlank()) { "Feature fingerprint is required for reproducibility" }
    }
}

/** Semantic version plus provenance for any model artifact, local or remote. */
public data class ModelVersion(
    val name: String,
    val version: String,
    val provider: ModelProvider,
) {
    init {
        require(name.isNotBlank() && version.isNotBlank()) { "Model name and version are required" }
    }

    override fun toString(): String = "$name@$version(${provider.name})"
}

public enum class ModelProvider {
    /** Deterministic heuristic shipped with the app. The honest baseline, never labelled as AI. */
    LocalBaseline,

    /** On-device inference, for example a quantized model executed through the NNAPI delegate. */
    OnDevice,

    /** Remote hosted inference. Optional by construction: the app must work without it. */
    RemoteHosted,
}

/** Immutable record closing the loop on a prediction. Written only after the horizon elapses. */
public data class PredictionOutcome(
    val predictionId: String,
    val realizedValue: Amount,
    val evaluatedAtEpochMillis: Long,
) {
    public fun error(expected: Amount): Amount = (realizedValue - expected).abs()
}
