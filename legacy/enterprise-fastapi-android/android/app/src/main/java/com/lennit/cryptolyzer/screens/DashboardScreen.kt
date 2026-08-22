package com.lennit.cryptolyzer.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lennit.cryptolyzer.domain.model.SystemMode
import com.lennit.cryptolyzer.ui.theme.*
import com.lennit.cryptolyzer.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.*

/** MODULE 02: Dashboard Screen */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: DashboardViewModel) {
    val status  by vm.status.collectAsState()
    val loading by vm.loading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚡ Dashboard", color = LennitCopper) },
                actions = {
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = LennitCopper)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LennitSurface),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (loading && status == null) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LennitCopper)
                }
            }

            status?.let { s ->
                // Portfolio card
                MetricCard(
                    title  = "Total Portfolio",
                    value  = "\$${NumberFormat.getNumberInstance(Locale.US).format(s.portfolioUsd)}",
                    sub    = "${if (s.pnl24h >= 0) "+" else ""}\$${
                        NumberFormat.getNumberInstance(Locale.US).format(s.pnl24h)
                    } (24h)",
                    valueColor = LennitGold,
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard("Active Agents", "${s.runningAgents}/${s.totalAgents}", "AlphaGrid",
                        modifier = Modifier.weight(1f))
                    MetricCard("System Mode", s.mode.name, "Status",
                        valueColor = if (s.mode == SystemMode.FULL_AUTONOMY) LennitGreen else LennitRed,
                        modifier = Modifier.weight(1f))
                }

                // Uptime
                val uptimeFmt = String.format("%02d:%02d:%02d",
                    s.uptimeSeconds / 3600, (s.uptimeSeconds % 3600) / 60, s.uptimeSeconds % 60)
                MetricCard("Uptime", uptimeFmt, "Sovereign Bargaining Loop")
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    sub: String = "",
    valueColor: androidx.compose.ui.graphics.Color = LennitWhite,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LennitSurface2),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = LennitGrey)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = valueColor)
            if (sub.isNotBlank()) {
                Text(sub, style = MaterialTheme.typography.bodySmall, color = LennitGrey)
            }
        }
    }
}
