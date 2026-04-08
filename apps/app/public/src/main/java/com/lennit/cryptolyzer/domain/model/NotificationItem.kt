package com.lennit.cryptolyzer.domain.model

data class NotificationItem(
    val id:        String,
    val timestamp: String,
    val level:     NotifLevel,
    val message:   String,
    val detail:    String = "",
)

enum class NotifLevel { INFO, WARN, ALERT, PROFIT }
