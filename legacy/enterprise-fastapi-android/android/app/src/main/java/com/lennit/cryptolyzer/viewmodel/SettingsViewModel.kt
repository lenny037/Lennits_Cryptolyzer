package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lennit.cryptolyzer.data.local.SettingsDataStore
import com.lennit.cryptolyzer.domain.model.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: SettingsDataStore,
) : ViewModel() {

    val config: StateFlow<AppConfig> = dataStore.appConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppConfig())

    fun saveApiEndpoint(v: String)   = viewModelScope.launch { dataStore.saveApiEndpoint(v) }
    fun saveApiKey(v: String)        = viewModelScope.launch { dataStore.saveApiKey(v) }
    fun saveDemoMode(v: Boolean)     = viewModelScope.launch { dataStore.saveDemoMode(v) }
    fun saveDeviation(v: Double)     = viewModelScope.launch { dataStore.saveDeviation(v) }
    fun savePollInterval(v: Double)  = viewModelScope.launch { dataStore.savePollInterval(v) }
    fun saveBiometric(v: Boolean)    = viewModelScope.launch { dataStore.saveBiometric(v) }
}
