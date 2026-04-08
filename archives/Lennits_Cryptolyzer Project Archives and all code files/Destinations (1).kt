package com.lennit.cryptolyzer.ui.navigation

enum class Destinations(val route: String, val label: String) {
    Dashboard("dashboard", "Dashboard"),
    Agents("agents", "Agents"),
    Vault("vault", "Vault"),
    Strategies("strategies", "Strategies"),
    Notifications("notifications", "Notifications"),
    Settings("settings", "Settings"),
    Safe("safe", "Safe")
}
