package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import com.lennit.cryptolyzer.core.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _demoMode = MutableStateFlow(AppConfig.demoMode)
    val demoMode: StateFlow<Boolean> = _demoMode

    val appVersion: String = "1.0.0"

    fun setDemoMode(enabled: Boolean) {
        AppConfig.demoMode = enabled
        _demoMode.value = enabled
    }
}
