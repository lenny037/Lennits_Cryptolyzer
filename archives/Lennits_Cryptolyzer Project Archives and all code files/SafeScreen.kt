package com.lennit.cryptolyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.lennit.cryptolyzer.domain.SystemStatus
import com.lennit.cryptolyzer.security.BiometricGatekeeper
import com.lennit.cryptolyzer.viewmodel.SafeControlViewModel

@Composable
fun SafeScreen(paddingValues: PaddingValues, viewModel: SafeControlViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)) {
        Text("SAFE / SHUTDOWN", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Current state: ${viewModel.status.value}")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Initiating SAFE SHUTDOWN requires biometric confirmation.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            if (activity != null) {
                val gatekeeper = BiometricGatekeeper(activity)
                if (gatekeeper.canAuthenticate()) {
                    gatekeeper.authenticate { success ->
                        if (success) {
                            viewModel.shutdown()
                        }
                    }
                }
            }
        }) {
            Text("Initiate SAFE SHUTDOWN")
        }
    }
}
