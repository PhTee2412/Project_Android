package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.SessionManager
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val onUnreadCountChanged: (Int) -> Unit
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val userId: String
        get() = sessionManager.getUserId() ?: ""

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = apiService.getNotifications(userId)
                val localRead = sessionManager.getReadNotifications()
                val merged = result.map { item ->
                    if (localRead.contains(item.id.toString())) item.copy(isRead = true) else item
                }.sortedByDescending { it.timestamp }
                _notifications.value = merged
                val unread = merged.count { !it.isRead }
                _unreadCount.value = unread
                onUnreadCountChanged(unread)
            } catch (e: Exception) {
                _message.value = "Không thể tải thông báo: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        sessionManager.addReadNotification(notificationId.toString())
        val currentList = _notifications.value.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        _notifications.value = currentList
        val unread = currentList.count { !it.isRead }
        _unreadCount.value = unread
        onUnreadCountChanged(unread)

        viewModelScope.launch {
            try {
                apiService.markNotificationAsRead(notificationId)
            } catch (_: Exception) { }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}