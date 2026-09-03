package com.lennit.cryptolyzer.core.finance

import com.lennit.cryptolyzer.core.modules.TreasurySnapshot
import kotlin.math.max

interface TreasurySource {
    suspend fun totalValueUsd(): Double
    suspend fun availableValueUsd(): Double
}

class TreasuryEngine(private val source: TreasurySource) {
    suspend fun snapshot(): TreasurySnapshot {
        val total = max(0.0, source.totalValueUsd())
        val available = source.availableValueUsd().coerceIn(0.0, total)
        return TreasurySnapshot(
            totalValueUsd = total,
            availableValueUsd = available,
            reservedValueUsd = total - available,
            asOfEpochMs = System.currentTimeMillis()
        )
    }
}
