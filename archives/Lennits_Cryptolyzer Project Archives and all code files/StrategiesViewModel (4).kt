package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lennit.cryptolyzer.data.repository.StrategiesRepository
import com.lennit.cryptolyzer.domain.StrategyConfig
import com.lennit.cryptolyzer.domain.StrategyMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class StrategiesViewModel @Inject constructor(
    private val repository: StrategiesRepository
) : ViewModel() {
    val strategies: StateFlow<List<StrategyConfig>> = repository.strategies
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateMode(id: String, mode: StrategyMode) = repository.updateMode(id, mode)
}
