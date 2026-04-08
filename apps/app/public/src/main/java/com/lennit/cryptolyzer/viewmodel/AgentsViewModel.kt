package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lennit.cryptolyzer.domain.model.Agent
import com.lennit.cryptolyzer.domain.repository.CryptolyzerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class AgentsViewModel @Inject constructor(
    @Named("mock") private val repo: CryptolyzerRepository,
) : ViewModel() {

    private val _agents  = MutableStateFlow<List<Agent>>(emptyList())
    private val _loading = MutableStateFlow(false)

    val agents:  StateFlow<List<Agent>> = _agents.asStateFlow()
    val loading: StateFlow<Boolean>     = _loading.asStateFlow()

    init { loadAgents() }

    fun loadAgents() {
        viewModelScope.launch {
            _loading.value = true
            try { _agents.value = repo.getAgents() }
            catch (e: Exception) { /* log */ }
            finally { _loading.value = false }
        }
    }

    fun controlAgent(agentId: String, action: String) {
        viewModelScope.launch {
            repo.controlAgent(agentId, action)
            loadAgents()
        }
    }
}
