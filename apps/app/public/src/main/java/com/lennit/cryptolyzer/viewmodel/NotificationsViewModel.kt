package com.lennit.cryptolyzer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lennit.cryptolyzer.domain.model.NotificationItem
import com.lennit.cryptolyzer.domain.repository.CryptolyzerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    @Named("mock") private val repo: CryptolyzerRepository,
) : ViewModel() {

    private val _items = MutableStateFlow<List<NotificationItem>>(emptyList())
    val items: StateFlow<List<NotificationItem>> = _items.asStateFlow()

    init { loadNotifications() }

    fun loadNotifications() {
        viewModelScope.launch { _items.value = repo.getNotifications(50) }
    }
}
