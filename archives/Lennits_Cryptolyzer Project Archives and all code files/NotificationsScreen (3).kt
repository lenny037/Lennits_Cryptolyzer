package com.lennit.cryptolyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lennit.cryptolyzer.domain.NotificationItem
import com.lennit.cryptolyzer.viewmodel.NotificationsViewModel

@Composable
fun NotificationsScreen(paddingValues: PaddingValues, viewModel: NotificationsViewModel = hiltViewModel()) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)) {
        Text("Notifications", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.notifications.value) { n ->
                NotificationRow(n)
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge)
            Text(item.message)
            Text("Severity: ${item.severity}")
            Text(if (item.isRead) "Read" else "Unread")
        }
    }
}
