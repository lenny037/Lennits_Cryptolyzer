package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lennit.cryptolyzer.domain.model.VaultPosition
import com.lennit.cryptolyzer.domain.repository.CryptolyzerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class VaultViewModel @Inject constructor(
    @Named("mock") private val repo: CryptolyzerRepository,
) : ViewModel() {

    private val _positions = MutableStateFlow<List<VaultPosition>>(emptyList())
    private val _loading   = MutableStateFlow(false)
    private val _totalUsd  = MutableStateFlow(0.0)

    val positions: StateFlow<List<VaultPosition>> = _positions.asStateFlow()
    val loading:   StateFlow<Boolean>             = _loading.asStateFlow()
    val totalUsd:  StateFlow<Double>              = _totalUsd.asStateFlow()

    init { loadVault() }

    fun loadVault() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val data = repo.getVaultPositions()
                _positions.value = data
                _totalUsd.value  = data.sumOf { it.usdValue }
            } finally { _loading.value = false }
        }
    }

    fun rebalance() {
        viewModelScope.launch { /* trigger via repo */ }
    }
}
