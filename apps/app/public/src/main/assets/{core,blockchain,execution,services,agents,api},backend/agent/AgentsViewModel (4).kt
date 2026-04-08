package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lennit.cryptolyzer.data.repository.AgentsRepository
import com.lennit.cryptolyzer.domain.Agent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AgentsViewModel @Inject constructor(
    private val repository: AgentsRepository
) : ViewModel() {
    val agents: StateFlow<List<Agent>> = repository.agents
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toggle(id: String) = repository.toggleAgent(id)
}
