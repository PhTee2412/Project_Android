package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.ChangePasswordRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangePasswordViewModel(private val apiService: ApiService) : ViewModel() {

    // User Data
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userAvatar = MutableStateFlow<String?>(null)
    val userAvatar: StateFlow<String?> = _userAvatar.asStateFlow()

    // Form Fields
    val currentPassword = MutableStateFlow("")
    val newPassword = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    // Password Visibility
    private val _showCurrentPassword = MutableStateFlow(false)
    val showCurrentPassword: StateFlow<Boolean> = _showCurrentPassword.asStateFlow()

    private val _showNewPassword = MutableStateFlow(false)
    val showNewPassword: StateFlow<Boolean> = _showNewPassword.asStateFlow()

    private val _showConfirmPassword = MutableStateFlow(false)
    val showConfirmPassword: StateFlow<Boolean> = _showConfirmPassword.asStateFlow()

    // UI State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    // Validation Errors
    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())
    val errors: StateFlow<Map<String, String>> = _errors.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = "user123" // Giả sử ID người dùng
                val response = apiService.getUserProfile(userId)
                if (response.status == "success" || response.data != null) {
                    _userName.value = response.data.fullname ?: "Người dùng"
                    _userEmail.value = response.data.email ?: ""
                    _userAvatar.value = response.data.avatar_url
                } else {
                    loadMockUser()
                }
            } catch (e: Exception) {
                loadMockUser()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadMockUser() {
        _userName.value = "Nguyễn Văn A"
        _userEmail.value = "vana@example.com"
        _userAvatar.value = null
    }

    fun togglePasswordVisibility(field: String) {
        when (field) {
            "current" -> _showCurrentPassword.value = !_showCurrentPassword.value
            "new" -> _showNewPassword.value = !_showNewPassword.value
            "confirm" -> _showConfirmPassword.value = !_showConfirmPassword.value
        }
    }

    private fun validateForm(): Boolean {
        val errorMap = mutableMapOf<String, String>()
        
        if (currentPassword.value.isBlank()) {
            errorMap["current"] = "Vui lòng nhập mật khẩu hiện tại"
        }
        
        if (newPassword.value.isBlank()) {
            errorMap["new"] = "Vui lòng nhập mật khẩu mới"
        } else if (newPassword.value.length < 6) {
            errorMap["new"] = "Mật khẩu mới phải có ít nhất 6 ký tự"
        } else if (newPassword.value == currentPassword.value) {
            errorMap["new"] = "Mật khẩu mới không được trùng mật khẩu cũ"
        }
        
        if (confirmPassword.value.isBlank()) {
            errorMap["confirm"] = "Vui lòng xác nhận mật khẩu mới"
        } else if (confirmPassword.value != newPassword.value) {
            errorMap["confirm"] = "Xác nhận mật khẩu không khớp"
        }

        _errors.value = errorMap
        return errorMap.isEmpty()
    }

    fun changePassword() {
        if (!validateForm()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = "user123"
                val request = ChangePasswordRequest(
                    id = userId,
                    oldPassword = currentPassword.value,
                    newPassword = newPassword.value
                )
                val response = apiService.changePassword(request)
                if (response.isSuccessful) {
                    _message.value = response.body()?.message ?: "Đổi mật khẩu thành công"
                    _isSuccess.value = true
                    resetForm()
                } else {
                    _message.value = "Đổi mật khẩu thất bại. Vui lòng kiểm tra lại."
                }
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun resetForm() {
        currentPassword.value = ""
        newPassword.value = ""
        confirmPassword.value = ""
        _errors.value = emptyMap()
    }

    fun clearMessage() {
        _message.value = null
    }
}
