package com.lennit.cryptolyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lennit.cryptolyzer.domain.Agent
import com.lennit.cryptolyzer.domain.AgentStatus
import com.lennit.cryptolyzer.viewmodel.AgentsViewModel

@Composable
fun AgentsScreen(paddingValues: PaddingValues, viewModel: AgentsViewModel = hiltViewModel()) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)) {
        Text("Agents", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.agents.value) { agent ->
                AgentRow(agent = agent, onToggle = { viewModel.toggle(agent.id) })
            }
        }
    }
}

@Composable
private fun AgentRow(agent: Agent, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(agent.name, style = MaterialTheme.typography.bodyLarge)
                Text(agent.status.name, style = MaterialTheme.typography.labelMedium)
            }
            Button(onClick = onToggle) {
                Text(
                    if (agent.status == AgentStatus.RUNNING) "Pause" else "Resume"
                )
            }
        }
    }
}
