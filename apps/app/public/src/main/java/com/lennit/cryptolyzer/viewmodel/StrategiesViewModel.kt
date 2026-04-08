package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lennit.cryptolyzer.domain.model.Strategy
import com.lennit.cryptolyzer.domain.repository.CryptolyzerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class StrategiesViewModel @Inject constructor(
    @Named("mock") private val repo: CryptolyzerRepository,
) : ViewModel() {

    private val _strategies = MutableStateFlow<List<Strategy>>(emptyList())
    val strategies: StateFlow<List<Strategy>> = _strategies.asStateFlow()

    init { loadStrategies() }

    fun loadStrategies() {
        viewModelScope.launch {
            _strategies.value = repo.getStrategies()
        }
    }

    fun setMode(strategyId: String, mode: String) {
        viewModelScope.launch {
            repo.updateStrategyMode(strategyId, mode)
            loadStrategies()
        }
    }
}
