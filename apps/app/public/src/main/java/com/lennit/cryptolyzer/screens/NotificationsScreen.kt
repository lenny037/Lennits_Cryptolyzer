package com.lennit.cryptolyzer.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lennit.cryptolyzer.domain.model.*
import com.lennit.cryptolyzer.ui.theme.*
import com.lennit.cryptolyzer.viewmodel.NotificationsViewModel

/** MODULE 06: Notifications Screen */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(vm: NotificationsViewModel) {
    val items by vm.items.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔔 Notifications", color = LennitCopper) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LennitSurface),
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            items(items, key = { it.id }) { NotificationCard(it) }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun NotificationCard(item: NotificationItem) {
    val (icon, color) = when (item.level) {
        NotifLevel.PROFIT -> "💰" to LennitGreen
        NotifLevel.INFO   -> "ℹ️" to LennitCopperLight
        NotifLevel.WARN   -> "⚠️" to LennitOrange
        NotifLevel.ALERT  -> "🚨" to LennitRed
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LennitSurface2),
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(icon, style = MaterialTheme.typography.titleMedium)
            Column(Modifier.weight(1f)) {
                Text(item.message, style = MaterialTheme.typography.bodyMedium, color = color)
                if (item.detail.isNotBlank()) {
                    Text(item.detail, style = MaterialTheme.typography.bodySmall, color = LennitGrey)
                }
                Text(item.timestamp.take(16).replace("T", " "),
                    style = MaterialTheme.typography.labelSmall, color = LennitGreyDark)
            }
        }
    }
}
