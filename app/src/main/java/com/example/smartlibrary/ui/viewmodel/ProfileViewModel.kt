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
import okhttp3.RequestBody.Companion.toRequestBody
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
                val user = apiService.getUserProfile(userId)
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
                _message.value = "Lỗi tải dữ liệu: ${e.localizedMessage}"
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
            _avatarFile.value = null
        }
    }

    fun onUserInfoChange(newInfo: UserInfo) {
        _userInfo.value = newInfo
    }

    fun onOtpChange(newOtp: String) {
        _otp.value = newOtp
    }

    fun setAvatarFile(file: File?) {
        _avatarFile.value = file
    }

    fun dismissOtp() {
        _isOtpSent.value = false
        _otp.value = ""
    }

    fun updateProfile() {
        val currentInfo = _userInfo.value ?: return
        if (userId.isEmpty()) return

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
                        _message.value = "Cập nhật thành công"
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

    fun uploadAvatar() {
        val file = _avatarFile.value
        if (file == null || userId.isEmpty()) {
            _message.value = "Vui lòng chọn ảnh trước"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val mediaType = when (file.extension.lowercase()) {
                    "png" -> "image/png"
                    "jpg", "jpeg" -> "image/jpeg"
                    "gif" -> "image/gif"
                    else -> "image/*"
                }.toMediaTypeOrNull()

                val requestFile = file.asRequestBody(mediaType)
                // Phải khớp với @RequestParam("file") ở Backend
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // Gửi ID dưới dạng RequestBody plain text
                val idPart = userId.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.uploadAvatar(idPart, filePart)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.data?.avatar_url != null) {
                        _userInfo.value = _userInfo.value?.copy(avatarUrl = responseBody.data.avatar_url)
                        _message.value = responseBody.message ?: "Upload ảnh thành công"
                        _avatarFile.value = null
                    } else {
                        _message.value = "Upload thất bại: Server không trả về URL ảnh"
                    }
                } else {
                    // Hiển thị lỗi chi tiết từ server để dễ debug
                    val errorJson = response.errorBody()?.string() ?: ""
                    _message.value = "Upload thất bại (${response.code()}): $errorJson"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi upload: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp() {
        if (_otp.value.length != 6 || userId.isEmpty()) return
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