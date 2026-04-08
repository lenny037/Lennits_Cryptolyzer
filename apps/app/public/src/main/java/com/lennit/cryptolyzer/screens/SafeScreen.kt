package com.lennit.cryptolyzer.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lennit.cryptolyzer.ui.theme.*
import com.lennit.cryptolyzer.viewmodel.SafeControlViewModel

/** MODULE 08: Safe Control / Emergency Screen */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeScreen(vm: SafeControlViewModel) {
    val mode by vm.systemMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🛡 Safe Control", color = LennitCopper) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LennitSurface),
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // System mode badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (mode == "FULL_AUTONOMY") LennitGreenDim else LennitSurface2
                ),
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current Mode", style = MaterialTheme.typography.labelMedium, color = LennitGrey)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        mode.replace("_", " "),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (mode == "FULL_AUTONOMY") LennitGreen else LennitRed,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Control buttons — require biometric via bridge
            Button(
                onClick  = { vm.activateSafeMode() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = LennitOrange),
            ) { Text("⏸  ACTIVATE SAFE MODE", style = MaterialTheme.typography.labelLarge) }

            Button(
                onClick  = { vm.resumeOperations() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = LennitGreen),
            ) { Text("▶  RESUME FULL AUTONOMY", style = MaterialTheme.typography.labelLarge) }

            Divider(color = LennitGreyDark)

            Button(
                onClick  = { vm.emergencyShutdown() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = LennitRed),
            ) { Text("🛑  EMERGENCY SHUTDOWN", style = MaterialTheme.typography.labelLarge) }

            Text(
                "⚠️ Emergency Shutdown immediately closes all positions and halts all agents. Requires biometric confirmation.",
                style = MaterialTheme.typography.bodySmall,
                color = LennitGrey,
                textAlign = TextAlign.Center,
            )
        }
    }
}
