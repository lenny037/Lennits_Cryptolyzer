package com.lennit.cryptolyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lennit.cryptolyzer.domain.VaultPosition
import com.lennit.cryptolyzer.viewmodel.VaultViewModel

@Composable
fun VaultScreen(paddingValues: PaddingValues, viewModel: VaultViewModel = hiltViewModel()) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)) {
        Text("Vault / Treasury", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.positions.value) { pos ->
                VaultRow(pos)
            }
        }
    }
}

@Composable
private fun VaultRow(position: VaultPosition) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${position.symbol} on ${position.chain}", style = MaterialTheme.typography.bodyLarge)
            Text("Balance: ${position.balance}")
            Text("USD: ${position.usdValue}")
        }
    }
}
