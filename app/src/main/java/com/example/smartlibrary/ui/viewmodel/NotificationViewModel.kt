package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val apiService: ApiService,
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

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Mock data if needed, but the requirement asks for API call
                val result = apiService.getNotifications(userId)
                _notifications.value = result.sortedByDescending { it.timestamp }
                
                val unread = result.count { !it.isRead }
                _unreadCount.value = unread
                onUnreadCountChanged(unread)
            } catch (e: Exception) {
                _message.value = "Lỗi khi tải thông báo: ${e.localizedMessage}"
                // fallback to mock data for demonstration
                val mockData = getMockNotifications()
                _notifications.value = mockData
                val unread = mockData.count { !it.isRead }
                _unreadCount.value = unread
                onUnreadCountChanged(unread)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.markNotificationAsRead(notificationId)
                if (response.isSuccessful) {
                    // Update locally for better UX
                    val currentList = _notifications.value.map {
                        if (it.id == notificationId) it.copy(isRead = true) else it
                    }
                    _notifications.value = currentList
                    
                    val unread = currentList.count { !it.isRead }
                    _unreadCount.value = unread
                    onUnreadCountChanged(unread)
                    
                    _message.value = "Đã đánh dấu đã đọc"
                } else {
                    _message.value = "Không thể cập nhật trạng thái"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.localizedMessage}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun getMockNotifications(): List<NotificationItem> {
        return listOf(
            NotificationItem("1", "Chào mừng bạn đến với SmartLibrary! 📚", "2023-10-27T10:00:00Z", false),
            NotificationItem("2", "Sách <b>Đắc Nhân Tâm</b> đã được trả thành công.", "2023-10-26T15:30:00Z", true),
            NotificationItem("3", "Bạn có yêu cầu mượn sách mới đang chờ duyệt.", "2023-10-25T09:15:00Z", false)
        )
    }
}
