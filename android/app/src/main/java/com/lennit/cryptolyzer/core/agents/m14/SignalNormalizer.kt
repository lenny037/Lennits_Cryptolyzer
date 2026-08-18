package com.lennit.cryptolyzer.core.agents.m14

import com.lennit.cryptolyzer.core.model.MarketSignal

class SignalNormalizer {
    fun normalize(signal: MarketSignal): MarketSignal = signal.copy(
        source = signal.source.trim().lowercase(),
        symbol = signal.symbol.trim().uppercase(),
        confidence = signal.confidence.coerceIn(0.0, 1.0)
    )

    fun normalizeAll(signals: List<MarketSignal>): List<MarketSignal> =
        signals.map(::normalize).distinctBy { "${it.source}:${it.symbol}:${it.observedAtEpochMs}" }
}
