package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.SessionManager
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.ChangePasswordRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangePasswordViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userAvatar = MutableStateFlow<String?>(null)
    val userAvatar: StateFlow<String?> = _userAvatar.asStateFlow()

    val currentPassword = MutableStateFlow("")
    val newPassword = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    private val _showCurrentPassword = MutableStateFlow(false)
    val showCurrentPassword: StateFlow<Boolean> = _showCurrentPassword.asStateFlow()

    private val _showNewPassword = MutableStateFlow(false)
    val showNewPassword: StateFlow<Boolean> = _showNewPassword.asStateFlow()

    private val _showConfirmPassword = MutableStateFlow(false)
    val showConfirmPassword: StateFlow<Boolean> = _showConfirmPassword.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())
    val errors: StateFlow<Map<String, String>> = _errors.asStateFlow()

    private val userId: String
        get() = sessionManager.getUserId() ?: ""

    init {
        loadUserData()
    }

    private fun loadUserData() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = apiService.getUserProfile(userId) // Nhận User trực tiếp
                _userName.value = user.fullname ?: "Người dùng"
                _userEmail.value = user.email ?: ""
                _userAvatar.value = user.avatar_url
            } catch (e: Exception) {
                _message.value = "Lỗi tải thông tin: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
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
        }

        if (confirmPassword.value != newPassword.value) {
            errorMap["confirm"] = "Xác nhận mật khẩu không khớp"
        }

        _errors.value = errorMap
        return errorMap.isEmpty()
    }

    fun changePassword() {
        if (!validateForm() || userId.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
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
                    _message.value = "Mật khẩu hiện tại không chính xác"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối: ${e.localizedMessage}"
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