package com.lennit.cryptolyzer.prediction

import com.lennit.cryptolyzer.intelligence.CanonicalSignal

data class Prediction(
    val predictionId: String,
    val modelId: String,
    val modelVersion: String,
    val featureSetVersion: String,
    val value: Double,
    val confidence: Double,
    val inferredAtEpochMs: Long,
    val provenance: List<String>
)

interface PredictionEngine {
    suspend fun predict(signals: List<CanonicalSignal>): Prediction
}
