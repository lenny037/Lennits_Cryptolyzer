package com.lennit.cryptolyzer.core.model

import org.junit.Test
import org.junit.Assert.assertThrows

class DomainModelsTest {
    @Test fun confidenceMustBeBounded() {
        assertThrows(IllegalArgumentException::class.java) {
            MarketSignal("test", "BTC", 0L, 1.1)
        }
    }

    @Test fun riskScoreMustBeBounded() {
        assertThrows(IllegalArgumentException::class.java) {
            Opportunity(OpportunityId("o"), "test", 1.0, -0.1, 0L)
        }
    }
}
