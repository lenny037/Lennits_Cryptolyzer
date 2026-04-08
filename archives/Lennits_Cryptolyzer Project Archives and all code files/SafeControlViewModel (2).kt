package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import com.lennit.cryptolyzer.data.repository.SystemRepository
import com.lennit.cryptolyzer.domain.SystemStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class SafeControlViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : ViewModel() {
    val status: StateFlow<SystemStatus> = systemRepository.status

    fun shutdown() = systemRepository.updateStatus(SystemStatus.SHUTDOWN)
}
