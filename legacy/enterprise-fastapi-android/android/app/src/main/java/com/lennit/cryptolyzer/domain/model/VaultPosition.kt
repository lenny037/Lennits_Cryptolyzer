package com.lennit.cryptolyzer.domain.model

data class VaultPosition(
    val symbol:   String,
    val name:     String,
    val amount:   Double,
    val usdValue: Double,
    val chain:    String  = "ETH",
    val apy:      Double? = null,
)
