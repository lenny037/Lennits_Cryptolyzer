package com.lennit.cryptolyzer.intelligence

data class CanonicalSignal(
    val signalId: String,
    val sourceObservationIds: List<String>,
    val assetId: String,
    val chainId: Long,
    val observedAtEpochMs: Long,
    val ingestedAtEpochMs: Long,
    val featureSetVersion: String,
    val values: Map<String, Double>,
    val qualityScore: Double,
    val provenance: String
)

interface SignalPipeline {
    fun extract(raw: String): List<String>
    fun normalize(observations: List<String>): List<String>
    fun analyze(normalized: List<String>): List<CanonicalSignal>
}
