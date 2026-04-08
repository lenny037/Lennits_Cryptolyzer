package com.lennit.cryptolyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lennit.cryptolyzer.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(navController: NavController, paddingValues: PaddingValues, viewModel: SettingsViewModel = hiltViewModel()) {
    val demoModeState = viewModel.demoMode.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Demo mode", style = MaterialTheme.typography.bodyLarge)
                Text("Use mock data only")
            }
            Switch(checked = demoModeState.value, onCheckedChange = { viewModel.setDemoMode(it) })
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("App version: ${viewModel.appVersion}")

        Spacer(modifier = Modifier.height(24.dp))

        Text("Connect Wallet (WalletConnect – coming soon)")
    }
}
