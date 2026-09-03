package com.lennit.cryptolyzer.core.analytics

import com.lennit.cryptolyzer.core.modules.Prediction

/** Deterministic on-device baseline. This is a fallback, not a trained financial model. */
class LocalBaselinePredictionModel : PredictionModel {
    override val version: String = "baseline-v1"

    override suspend fun predict(subject: String, horizonSeconds: Long): Prediction {
        require(subject.isNotBlank())
        require(horizonSeconds > 0)
        return Prediction(
            subject = subject,
            probability = 0.5,
            horizonSeconds = horizonSeconds,
            modelVersion = version
        )
    }
}
