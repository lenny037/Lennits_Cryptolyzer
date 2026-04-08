package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lennit.cryptolyzer.domain.repository.CryptolyzerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SafeControlViewModel @Inject constructor(
    @Named("mock") private val repo: CryptolyzerRepository,
) : ViewModel() {

    private val _systemMode = MutableStateFlow("FULL_AUTONOMY")
    private val _actionResult = MutableSharedFlow<String>()

    val systemMode:   StateFlow<String>    = _systemMode.asStateFlow()
    val actionResult: SharedFlow<String>   = _actionResult.asSharedFlow()

    fun activateSafeMode() {
        viewModelScope.launch {
            val ok = repo.activateSafeMode()
            if (ok) {
                _systemMode.value = "SAFE_MODE"
                _actionResult.emit("Safe mode activated — agents paused")
            }
        }
    }

    fun emergencyShutdown() {
        viewModelScope.launch {
            val ok = repo.emergencyShutdown()
            if (ok) {
                _systemMode.value = "SHUTDOWN"
                _actionResult.emit("Emergency shutdown initiated")
            }
        }
    }

    fun resumeOperations() {
        viewModelScope.launch {
            val ok = repo.resumeOperations()
            if (ok) {
                _systemMode.value = "FULL_AUTONOMY"
                _actionResult.emit("Operations resumed")
            }
        }
    }
}
