package com.lennit.cryptolyzer.domain.model

data class AppConfig(
    val apiEndpoint:    String  = "",
    val apiKey:         String  = "",
    val useDemoMode:    Boolean = false,
    val deviationThreshold: Double = 5.0,
    val pollIntervalSeconds: Double = 2.0,
    val maxPositionSizeUsd: Double = 10_000.0,
    val isDarkMode:     Boolean = true,
    val biometricEnabled: Boolean = true,
)
