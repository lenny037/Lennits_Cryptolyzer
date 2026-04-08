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
import com.lennit.cryptolyzer.viewmodel.StrategiesViewModel

/** MODULE 05: Strategies Screen */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategiesScreen(vm: StrategiesViewModel) {
    val strategies by vm.strategies.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Strategies", color = LennitCopper) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LennitSurface),
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(strategies, key = { it.id }) { strategy ->
                StrategyCard(strategy = strategy, onSetMode = { mode -> vm.setMode(strategy.id, mode) })
            }
        }
    }
}

@Composable
fun StrategyCard(strategy: Strategy, onSetMode: (String) -> Unit) {
    val typeColor = when (strategy.type) {
        StrategyType.ARBITRAGE   -> LennitGold
        StrategyType.YIELD       -> LennitGreen
        StrategyType.AIRDROP     -> LennitCopperLight
        StrategyType.MEV         -> LennitRed
        StrategyType.CROSS_CHAIN -> LennitOrange
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LennitSurface2),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(strategy.name, style = MaterialTheme.typography.titleSmall, color = LennitWhite)
                    Surface(color = typeColor.copy(0.15f), shape = MaterialTheme.shapes.small) {
                        Text(strategy.type.name, Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, color = typeColor)
                    }
                }
                StrategyModeChip(strategy.mode)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Allocated", style = MaterialTheme.typography.labelSmall, color = LennitGrey)
                    Text("\$${"%,.0f".format(strategy.allocatedUsd)}", color = LennitWhite,
                        style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("24h PnL", style = MaterialTheme.typography.labelSmall, color = LennitGrey)
                    Text("+\$${"%,.2f".format(strategy.pnl24h)}", color = LennitGreen,
                        style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Win Rate", style = MaterialTheme.typography.labelSmall, color = LennitGrey)
                    Text("${(strategy.winRate * 100).toInt()}%", color = LennitCopperLight,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("LIVE", "SHADOW", "OFF").forEach { mode ->
                    val selected = strategy.mode.name == mode
                    OutlinedButton(
                        onClick = { onSetMode(mode) },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = if (selected)
                            ButtonDefaults.filledTonalButtonColors()
                        else
                            ButtonDefaults.outlinedButtonColors(contentColor = LennitGrey),
                    ) { Text(mode, style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
    }
}

@Composable
fun StrategyModeChip(mode: StrategyMode) {
    val (color, label) = when (mode) {
        StrategyMode.LIVE   -> LennitGreen   to "● LIVE"
        StrategyMode.SHADOW -> LennitOrange  to "◐ SHADOW"
        StrategyMode.OFF    -> LennitGrey    to "○ OFF"
    }
    Surface(color = color.copy(0.15f), shape = MaterialTheme.shapes.small) {
        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall, color = color)
    }
}
