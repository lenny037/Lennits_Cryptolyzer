package com.lennit.cryptolyzer.domain

import com.lennit.cryptolyzer.contracts.Outcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PredictionTest {

    private val modelVersion = ModelVersion(
        name = "ev-ranker",
        version = "0.3.1",
        provider = ModelProvider.LocalBaseline,
    )

    private fun prediction(
        confidence: Confidence = Confidence.of(0.62).expect(),
        expectedValue: Amount = amount("12.50"),
        fingerprint: String = "sha256:abc",
        horizon: Long = 3_600_000,
    ) = Prediction(
        id = "p1",
        modelVersion = modelVersion,
        subject = "ETH",
        horizonMillis = horizon,
        expectedValue = expectedValue,
        confidence = confidence,
        createdAtEpochMillis = 1_000,
        featureFingerprint = fingerprint,
    )

    @Test
    fun `confidence is confined to the unit interval`() {
        assertTrue(Confidence.of(BigDecimal("1.01")) is Outcome.Failure)
        assertTrue(Confidence.of(BigDecimal("-0.01")) is Outcome.Failure)
        assertEquals(Confidence.ZERO, Confidence.of(0.0).expect())
        assertEquals(Confidence.ONE, Confidence.of(1.0).expect())
    }

    @Test
    fun `confidence equality ignores scale and renders as a percentage`() {
        assertEquals(Confidence.of(BigDecimal("0.50")).expect(), Confidence.of(BigDecimal("0.5")).expect())
        assertEquals(0, Confidence.of(0.625).expect().asPercent().compareTo(BigDecimal("62.5")))
    }

    @Test
    fun `confidence is ordered so calibration bands can be compared`() {
        val sorted = listOf(Confidence.ONE, Confidence.ZERO, Confidence.of(0.5).expect()).sorted()
        assertEquals(listOf(Confidence.ZERO, Confidence.of(0.5).expect(), Confidence.ONE), sorted)
    }

    @Test
    fun `a prediction without a feature fingerprint cannot exist, because it is not reproducible`() {
        assertThrows(IllegalArgumentException::class.java) { prediction(fingerprint = " ") }
    }

    @Test
    fun `a prediction without a positive horizon cannot exist, because it can never be evaluated`() {
        assertThrows(IllegalArgumentException::class.java) { prediction(horizon = 0) }
        assertThrows(IllegalArgumentException::class.java) { prediction(horizon = -1) }
    }

    @Test
    fun `every prediction carries the exact model version that produced it`() {
        assertEquals("ev-ranker@0.3.1(LocalBaseline)", prediction().modelVersion.toString())
    }

    @Test
    fun `a model version must be identifiable`() {
        assertThrows(IllegalArgumentException::class.java) { modelVersion.copy(name = "") }
        assertThrows(IllegalArgumentException::class.java) { modelVersion.copy(version = " ") }
    }

    @Test
    fun `outcome error is the absolute distance from the prediction, in either direction`() {
        val outcome = PredictionOutcome(
            predictionId = "p1",
            realizedValue = amount("10"),
            evaluatedAtEpochMillis = 5_000,
        )
        assertEquals(amount("2.50"), outcome.error(amount("12.50")))
        assertEquals(amount("2.50"), outcome.error(amount("7.50")))
        assertEquals(Amount.ZERO, outcome.error(amount("10")))
    }

    @Test
    fun `the honest baseline is a distinct provider from on-device inference`() {
        assertEquals(3, ModelProvider.entries.size)
        assertTrue(ModelProvider.entries.contains(ModelProvider.LocalBaseline))
    }
}
