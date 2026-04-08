package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import com.lennit.cryptolyzer.data.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class DashboardViewModel @Inject constructor(
    systemRepository: SystemRepository
) : ViewModel() {
    val systemStatus: StateFlow<com.lennit.cryptolyzer.domain.SystemStatus> = systemRepository.status

    val totalPortfolioUsd: String = "$155,000"
    val pnl24hUsd: String = "+$3,420"
    val activeAgents: Int = 2
}
