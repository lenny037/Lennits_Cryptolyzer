package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lennit.cryptolyzer.data.repository.VaultRepository
import com.lennit.cryptolyzer.domain.VaultPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class VaultViewModel @Inject constructor(
    repository: VaultRepository
) : ViewModel() {
    val positions: StateFlow<List<VaultPosition>> = repository.positions
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
