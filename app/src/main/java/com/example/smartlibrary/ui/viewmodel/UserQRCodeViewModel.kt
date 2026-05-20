package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.SessionManager
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserQRInfo(
    val id: String,
    val fullname: String,
    val email: String,
    val username: String,
    val avatarUrl: String?
)

class UserQRCodeViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _userInfo = MutableStateFlow<UserQRInfo?>(null)
    val userInfo: StateFlow<UserQRInfo?> = _userInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val userId: String
        get() = sessionManager.getUserId() ?: ""

    init {
        if (userId.isNotEmpty()) {
            loadUserData()
        }
    }

    fun loadUserData() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val user = apiService.getUserProfile(userId) // Trả về User trực tiếp
                _userInfo.value = UserQRInfo(
                    id = user.id.toString(),
                    fullname = user.fullname ?: "N/A",
                    email = user.email ?: "N/A",
                    username = user.username ?: "N/A",
                    avatarUrl = user.avatar_url
                )
            } catch (e: Exception) {
                _error.value = "Lỗi tải thông tin: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}