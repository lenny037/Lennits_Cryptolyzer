package com.lennit.cryptolyzer.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.lennit.cryptolyzer.ui.theme.*
import com.lennit.cryptolyzer.viewmodel.SettingsViewModel

/** MODULE 07: Settings Screen */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val config by vm.config.collectAsState()
    var endpoint  by remember(config.apiEndpoint)  { mutableStateOf(config.apiEndpoint) }
    var apiKey    by remember(config.apiKey)        { mutableStateOf(config.apiKey) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Settings", color = LennitCopper) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LennitSurface),
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SettingSection("Connection") {
                OutlinedTextField(
                    value          = endpoint,
                    onValueChange  = { endpoint = it },
                    label          = { Text("API Endpoint") },
                    placeholder    = { Text("http://your-server:8000") },
                    modifier       = Modifier.fillMaxWidth(),
                    singleLine     = true,
                )
                OutlinedTextField(
                    value                  = apiKey,
                    onValueChange          = { apiKey = it },
                    label                  = { Text("API Key") },
                    visualTransformation   = PasswordVisualTransformation(),
                    keyboardOptions        = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier               = Modifier.fillMaxWidth(),
                    singleLine             = true,
                )
                Button(
                    onClick  = { vm.saveApiEndpoint(endpoint); vm.saveApiKey(apiKey) },
                    modifier = Modifier.align(Alignment.End),
                    colors   = ButtonDefaults.buttonColors(containerColor = LennitCopper),
                ) { Text("Save") }
            }

            SettingSection("Mode") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Demo Mode", color = LennitWhite)
                    Switch(
                        checked         = config.useDemoMode,
                        onCheckedChange = { vm.saveDemoMode(it) },
                        colors          = SwitchDefaults.colors(checkedThumbColor = LennitCopper),
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Biometric Auth", color = LennitWhite)
                    Switch(
                        checked         = config.biometricEnabled,
                        onCheckedChange = { vm.saveBiometric(it) },
                        colors          = SwitchDefaults.colors(checkedThumbColor = LennitCopper),
                    )
                }
            }

            SettingSection("Trading") {
                Text("Deviation Threshold: ${config.deviationThreshold}%",
                    color = LennitGrey, style = MaterialTheme.typography.bodySmall)
                Slider(
                    value          = config.deviationThreshold.toFloat(),
                    onValueChange  = { vm.saveDeviation(it.toDouble()) },
                    valueRange     = 0.5f..20f,
                    colors         = SliderDefaults.colors(thumbColor = LennitCopper, activeTrackColor = LennitCopper),
                )
                Text("Poll Interval: ${config.pollIntervalSeconds}s",
                    color = LennitGrey, style = MaterialTheme.typography.bodySmall)
                Slider(
                    value          = config.pollIntervalSeconds.toFloat(),
                    onValueChange  = { vm.savePollInterval(it.toDouble()) },
                    valueRange     = 0.5f..30f,
                    colors         = SliderDefaults.colors(thumbColor = LennitCopper, activeTrackColor = LennitCopper),
                )
            }
        }
    }
}

@Composable
fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = LennitSurface2)) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = LennitCopper)
            Divider(color = LennitGreyDark)
            content()
        }
    }
}
