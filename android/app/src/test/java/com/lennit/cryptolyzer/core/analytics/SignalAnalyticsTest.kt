package com.lennit.cryptolyzer.core.analytics

import com.lennit.cryptolyzer.core.model.MarketSignal
import org.junit.Assert.assertEquals
import org.junit.Test

class SignalAnalyticsTest {
    @Test fun calculatesConfidenceAndLatestSignals() {
        val signals = listOf(
            MarketSignal("a", "ETH", 1L, 0.4),
            MarketSignal("b", "ETH", 2L, 0.8),
            MarketSignal("a", "BTC", 3L, 1.0)
        )
        val analytics = SignalAnalytics()
        assertEquals(0.733333, analytics.confidenceAverage(signals), 0.00001)
        assertEquals(2L, analytics.latestBySymbol(signals)["ETH"]!!.observedAtEpochMs)
        assertEquals(2, analytics.countBySymbol(signals).size)
    }
}
