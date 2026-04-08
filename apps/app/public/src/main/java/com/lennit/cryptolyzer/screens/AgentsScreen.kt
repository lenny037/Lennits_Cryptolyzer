package com.lennit.cryptolyzer.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lennit.cryptolyzer.domain.model.*
import com.lennit.cryptolyzer.ui.theme.*
import com.lennit.cryptolyzer.viewmodel.AgentsViewModel

/** MODULE 03: Agents Screen — 20-agent AlphaGrid monitor */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(vm: AgentsViewModel) {
    val agents  by vm.agents.collectAsState()
    val loading by vm.loading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🤖 AlphaGrid — ${agents.count { it.status == AgentStatus.RUNNING }}/20", color = LennitCopper) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LennitSurface),
            )
        },
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LennitCopper)
            }
        } else {
            LazyColumn(
                Modifier.padding(padding).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(agents, key = { it.id }) { agent ->
                    AgentCard(agent = agent, onControl = { action -> vm.controlAgent(agent.id, action) })
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun AgentCard(agent: Agent, onControl: (String) -> Unit) {
    val roleColor = when (agent.role) {
        AgentRole.ARBITRAGE  -> LennitGold
        AgentRole.HIMAP      -> LennitCopperLight
        AgentRole.DARKFOREST -> LennitRed
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LennitSurface2),
    ) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(agent.id, style = MaterialTheme.typography.labelLarge, color = LennitWhite)
                    Surface(color = roleColor.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) {
                        Text(
                            agent.role.name, Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, color = roleColor,
                        )
                    }
                }
                Text("+\$${String.format("%.2f", agent.profitUsd)}", color = LennitGreen,
                    style = MaterialTheme.typography.bodyMedium)
                Text(agent.lastAction, color = LennitGrey, style = MaterialTheme.typography.bodySmall)
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusChip(agent.status)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (agent.status != AgentStatus.RUNNING) {
                        SmallButton("▶", LennitGreen) { onControl("START") }
                    } else {
                        SmallButton("⏸", LennitOrange) { onControl("PAUSE") }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: AgentStatus) {
    val (color, label) = when (status) {
        AgentStatus.RUNNING -> LennitGreen   to "RUNNING"
        AgentStatus.PAUSED  -> LennitOrange  to "PAUSED"
        AgentStatus.ERROR   -> LennitRed     to "ERROR"
        AgentStatus.STOPPED -> LennitGrey    to "STOPPED"
    }
    Surface(color = color.copy(0.15f), shape = MaterialTheme.shapes.small) {
        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun SmallButton(label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(28.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(color.copy(0.5f))
        ),
    ) { Text(label, style = MaterialTheme.typography.labelSmall) }
}
