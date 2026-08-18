package com.lennit.cryptolyzer.core.analytics

import com.lennit.cryptolyzer.core.model.MarketSignal

class SignalAnalytics {
    fun confidenceAverage(signals: List<MarketSignal>): Double =
        signals.map { it.confidence }.average().takeIf { !it.isNaN() } ?: 0.0

    fun countBySymbol(signals: List<MarketSignal>): Map<String, Int> =
        signals.groupingBy { it.symbol }.eachCount()

    fun latestBySymbol(signals: List<MarketSignal>): Map<String, MarketSignal> =
        signals.groupBy { it.symbol }.mapValues { (_, values) -> values.maxBy { it.observedAtEpochMs } }
}
