package com.lennit.cryptolyzer.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lennit.cryptolyzer.domain.model.VaultPosition
import com.lennit.cryptolyzer.ui.theme.*
import com.lennit.cryptolyzer.viewmodel.VaultViewModel

/** MODULE 04: Treasury / Vault Screen */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(vm: VaultViewModel) {
    val positions by vm.positions.collectAsState()
    val totalUsd  by vm.totalUsd.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏛 Treasury Vault", color = LennitCopper) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LennitSurface),
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                MetricCard(
                    title = "Total Vault Value",
                    value = "\$${"%,.2f".format(totalUsd)}",
                    sub   = "${positions.size} assets across ${positions.map { it.chain }.distinct().size} chains",
                    valueColor = LennitGold,
                )
            }
            items(positions, key = { it.symbol }) { pos -> VaultPositionCard(pos) }
        }
    }
}

@Composable
fun VaultPositionCard(pos: VaultPosition) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LennitSurface2),
    ) {
        Row(
            Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(pos.symbol, style = MaterialTheme.typography.titleMedium, color = LennitCopperLight)
                Text(pos.name,   style = MaterialTheme.typography.bodySmall,   color = LennitGrey)
                Text("${pos.amount} • ${pos.chain}", style = MaterialTheme.typography.labelSmall, color = LennitGrey)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("\$${"%,.2f".format(pos.usdValue)}", color = LennitWhite,
                    style = MaterialTheme.typography.titleMedium)
                pos.apy?.let { apy ->
                    Text("APY: ${String.format("%.1f", apy)}%", color = LennitGreen,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
