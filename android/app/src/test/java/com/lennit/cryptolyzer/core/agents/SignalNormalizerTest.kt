package com.lennit.cryptolyzer.core.agents

import com.lennit.cryptolyzer.core.agents.m14.SignalNormalizer
import com.lennit.cryptolyzer.core.model.MarketSignal
import org.junit.Assert.assertEquals
import org.junit.Test

class SignalNormalizerTest {
    @Test fun normalizesIdentityAndDeduplicates() {
        val input = listOf(
            MarketSignal(" EXAMPLE ", "eth", 10L, 0.8),
            MarketSignal("example", "ETH", 10L, 0.8)
        )
        val output = SignalNormalizer().normalizeAll(input)
        assertEquals(1, output.size)
        assertEquals("example", output.first().source)
        assertEquals("ETH", output.first().symbol)
    }
}
