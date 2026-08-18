package com.lennit.cryptolyzer.core.analytics

import com.lennit.cryptolyzer.core.modules.Prediction

interface PredictionModel {
    val version: String
    suspend fun predict(subject: String, horizonSeconds: Long): Prediction
}

class PredictionEngine(private val model: PredictionModel) {
    suspend fun predict(subject: String, horizonSeconds: Long): Prediction {
        require(subject.isNotBlank())
        require(horizonSeconds > 0)
        val prediction = model.predict(subject.trim(), horizonSeconds)
        require(prediction.probability in 0.0..1.0)
        require(prediction.modelVersion.isNotBlank())
        return prediction
    }
}
