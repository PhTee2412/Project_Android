package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
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

class UserQRCodeViewModel(private val apiService: ApiService) : ViewModel() {
    private val _userInfo = MutableStateFlow<UserQRInfo?>(null)
    val userInfo: StateFlow<UserQRInfo?> = _userInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserData("user123") // Mock userId
    }

    fun loadUserData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.getUserProfile(userId)
                if (response.status == "success" || response.data != null) {
                    val data = response.data
                    _userInfo.value = UserQRInfo(
                        id = data.id?.toString() ?: "N/A",
                        fullname = data.fullname ?: "N/A",
                        email = data.email ?: "N/A",
                        username = data.username ?: "N/A",
                        avatarUrl = data.avatar_url
                    )
                } else {
                    useMockData()
                }
            } catch (e: Exception) {
                // Fallback to mock data if API fails
                useMockData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun useMockData() {
        _userInfo.value = UserQRInfo(
            id = "23521468",
            fullname = "Lê Thị Phương Thảo",
            email = "phtee2412@gmail.com",
            username = "PhTee",
            avatarUrl = null
        )
    }
}
