package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.model.UserInfo
import com.example.smartlibrary.network.ApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(private val apiService: ApiService) : ViewModel() {

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    private val _avatarFile = MutableStateFlow<File?>(null)
    val avatarFile: StateFlow<File?> = _avatarFile.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Giả lập gọi API với delay
                delay(1000)
                // Mock data
                _userInfo.value = UserInfo(
                    fullName = "Nguyễn Văn A",
                    email = "vana@example.com",
                    phone = "0123456789",
                    birthdate = "2000-01-01",
                    joinDate = "2024-01-15",
                    avatarUrl = null,
                    studentId = "23521468"
                )
            } catch (e: Exception) {
                _message.value = "Không thể tải thông tin người dùng"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleEdit() {
        _isEditing.value = !_isEditing.value
        if (!_isEditing.value) {
            _isOtpSent.value = false
            _otp.value = ""
            _message.value = null
        }
    }

    fun onUserInfoChange(newInfo: UserInfo) {
        _userInfo.value = newInfo
    }

    fun onOtpChange(newOtp: String) {
        _otp.value = newOtp
    }

    fun dismissOtp() {
        _isOtpSent.value = false
        _otp.value = ""
    }

    fun updateProfile() {
        val currentInfo = _userInfo.value ?: return
        
        // Validate đơn giản
        if (currentInfo.fullName.isBlank()) {
            _message.value = "Họ và tên không được để trống"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                delay(1500) // Giả lập network
                
                // Giả lập logic: nếu email thay đổi thì yêu cầu OTP
                // Ở đây giả sử email gốc là vana@example.com
                if (currentInfo.email != "vana@example.com" && !_isOtpSent.value) {
                    _isOtpSent.value = true
                    _message.value = "Vui lòng nhập OTP được gửi tới email mới"
                } else {
                    // Cập nhật thành công
                    _message.value = "Cập nhật thông tin thành công"
                    _isEditing.value = false
                    _isOtpSent.value = false
                }
            } catch (e: Exception) {
                _message.value = "Có lỗi xảy ra khi cập nhật"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadAvatar(file: File) {
        _avatarFile.value = file
        viewModelScope.launch {
            _isLoading.value = true
            try {
                delay(2000)
                // Giả lập upload thành công và nhận URL mới
                _userInfo.value = _userInfo.value?.copy(avatarUrl = "https://example.com/new_avatar.png")
                _message.value = "Upload ảnh đại diện thành công"
                _avatarFile.value = null
            } catch (e: Exception) {
                _message.value = "Lỗi khi upload ảnh"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp() {
        if (_otp.value.length != 6) {
            _message.value = "Vui lòng nhập mã OTP hợp lệ (6 chữ số)"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                delay(1000)
                // Giả lập xác thực thành công
                _message.value = "Xác thực email thành công"
                _isOtpSent.value = false
                _isEditing.value = false
                _otp.value = ""
            } catch (e: Exception) {
                _message.value = "Xác thực OTP thất bại"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
