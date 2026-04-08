package com.lennit.cryptolyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lennit.cryptolyzer.domain.SystemStatus
import com.lennit.cryptolyzer.ui.navigation.Destinations
import com.lennit.cryptolyzer.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(navController: NavController, paddingValues: PaddingValues, viewModel: DashboardViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("CRYPTOLYZER Operations Console", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Portfolio", style = MaterialTheme.typography.labelMedium)
                Text(viewModel.totalPortfolioUsd, style = MaterialTheme.typography.headlineSmall)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("24h PnL", style = MaterialTheme.typography.labelMedium)
                Text(viewModel.pnl24hUsd, style = MaterialTheme.typography.headlineSmall)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Active agents", style = MaterialTheme.typography.labelMedium)
                Text(viewModel.activeAgents.toString(), style = MaterialTheme.typography.headlineSmall)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("System status", style = MaterialTheme.typography.labelMedium)
                Text(
                    when (viewModel.systemStatus.value) {
                        SystemStatus.OK -> "OK"
                        SystemStatus.SHUTDOWN -> "SHUTDOWN"
                    },
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { navController.navigate(Destinations.Safe.route) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("SAFE / SHUTDOWN")
        }
    }
}
