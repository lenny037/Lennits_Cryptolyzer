package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lennit.cryptolyzer.domain.model.SystemStatus
import com.lennit.cryptolyzer.domain.repository.CryptolyzerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @Named("mock") private val repo: CryptolyzerRepository,
) : ViewModel() {

    private val _status  = MutableStateFlow<SystemStatus?>(null)
    private val _loading = MutableStateFlow(false)
    private val _error   = MutableStateFlow<String?>(null)

    val status:  StateFlow<SystemStatus?> = _status.asStateFlow()
    val loading: StateFlow<Boolean>       = _loading.asStateFlow()
    val error:   StateFlow<String?>       = _error.asStateFlow()

    init { startPolling() }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                refresh()
                delay(5_000L)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _status.value = repo.getDashboard()
                _error.value  = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
