package com.lennit.cryptolyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lennit.cryptolyzer.domain.StrategyConfig
import com.lennit.cryptolyzer.domain.StrategyMode
import com.lennit.cryptolyzer.viewmodel.StrategiesViewModel

@Composable
fun StrategiesScreen(paddingValues: PaddingValues, viewModel: StrategiesViewModel = hiltViewModel()) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)) {
        Text("Strategies", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.strategies.value) { s ->
                StrategyRow(strategy = s, onModeChange = { mode -> viewModel.updateMode(s.id, mode) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrategyRow(strategy: StrategyConfig, onModeChange: (StrategyMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = StrategyMode.values().toList()
    var selected by remember { mutableStateOf(strategy.mode) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(strategy.name, style = MaterialTheme.typography.bodyLarge)
            Text("Type: ${strategy.type}")
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                TextField(
                    value = selected.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )
                androidx.compose.material3.ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name) },
                            onClick = {
                                selected = mode
                                expanded = false
                                onModeChange(mode)
                            }
                        )
                    }
                }
            }
        }
    }
}
