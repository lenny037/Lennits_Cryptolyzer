package com.lennit.cryptolyzer.core.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SystemMode { OBSERVE, PAPER, SHADOW, SIMULATE, MANUAL_APPROVAL, CONTROLLED_LIVE, SAFE_MODE, SHUTDOWN }

data class SystemState(
    val mode: SystemMode = SystemMode.OBSERVE,
    val networkOnline: Boolean = false,
    val agentsReady: Int = 0,
    val agentsTotal: Int = 23,
    val securityReady: Boolean = true,
    val localDatabaseReady: Boolean = true
)

class SystemStateStore {
    private val state = MutableStateFlow(SystemState())
    val flow: StateFlow<SystemState> = state.asStateFlow()

    fun setMode(mode: SystemMode) {
        state.value = state.value.copy(mode = mode)
    }

    fun setNetwork(online: Boolean) {
        state.value = state.value.copy(networkOnline = online)
    }
}
