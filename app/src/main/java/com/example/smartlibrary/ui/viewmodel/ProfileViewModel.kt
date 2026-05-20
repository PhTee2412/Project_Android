package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.SessionManager
import com.example.smartlibrary.data.model.UserInfo
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.VerifyOtpRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

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

    private val userId: String
        get() = sessionManager.getUserId() ?: ""

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = apiService.getUserProfile(userId)   // Nhận User
                _userInfo.value = UserInfo(
                    fullName = user.fullname ?: "",
                    email = user.email ?: "",
                    phone = user.phone ?: "",
                    birthdate = user.birthdate,
                    joinDate = user.joined_date,
                    avatarUrl = user.avatar_url,
                    studentId = user.id.toString()
                )
            } catch (e: Exception) {
                _message.value = "Không thể tải thông tin người dùng: ${e.localizedMessage}"
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
        if (userId.isEmpty()) return
        
        if (currentInfo.fullName.isBlank()) {
            _message.value = "Họ và tên không được để trống"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updates = mutableMapOf<String, String?>()
                updates["fullname"] = currentInfo.fullName
                updates["phone"] = currentInfo.phone
                updates["birthdate"] = currentInfo.birthdate
                updates["email"] = currentInfo.email

                val response = apiService.updateUserProfile(userId, updates)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "otp_sent") {
                        _isOtpSent.value = true
                        _message.value = "Vui lòng nhập OTP được gửi tới email mới"
                    } else {
                        _message.value = "Cập nhật thông tin thành công"
                        _isEditing.value = false
                        loadUserProfile()
                    }
                } else {
                    _message.value = "Cập nhật thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadAvatar(file: File) {
        if (userId.isEmpty()) return
        _avatarFile.value = file
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id", userId)
                    .addFormDataPart("avatar", file.name, requestFile)
                    .build()

                val response = apiService.uploadAvatar(body)
                if (response.isSuccessful) {
                    _userInfo.value = _userInfo.value?.copy(avatarUrl = response.body()?.data?.avatar_url)
                    _message.value = "Upload ảnh đại diện thành công"
                    _avatarFile.value = null
                } else {
                    _message.value = "Upload thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi upload: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp() {
        if (_otp.value.length != 6 || userId.isEmpty()) {
            _message.value = "Vui lòng nhập mã OTP hợp lệ"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.verifyEmailUpdate(
                    VerifyOtpRequest(id = userId, email = _userInfo.value?.email ?: "", otp = _otp.value)
                )
                if (response.isSuccessful) {
                    _message.value = "Xác thực email thành công"
                    _isOtpSent.value = false
                    _isEditing.value = false
                    _otp.value = ""
                    loadUserProfile()
                } else {
                    _message.value = "Xác thực thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi xác thực: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
