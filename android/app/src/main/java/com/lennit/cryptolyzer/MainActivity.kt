package com.lennit.cryptolyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lennit.cryptolyzer.core.model.SystemStateStore
import com.lennit.cryptolyzer.core.model.SystemMode

class MainActivity : ComponentActivity() {
    private val systemStateStore = SystemStateStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by systemStateStore.flow.collectAsState()
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Lennits Cryptolyzer", style = MaterialTheme.typography.headlineMedium)
                        Text("Mobile Core v0.1.0")
                        Text("Mode: ${state.mode}")
                        Text("Network: ${if (state.networkOnline) "ONLINE" else "OFFLINE"}")
                        Text("Agents: ${state.agentsReady}/${state.agentsTotal}")
                        Text("Local database: ${if (state.localDatabaseReady) "READY" else "ERROR"}")
                        Text("Security: ${if (state.securityReady) "READY" else "ERROR"}")
                        if (state.mode == SystemMode.SAFE_MODE) {
                            Text("SAFE MODE ACTIVE", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
