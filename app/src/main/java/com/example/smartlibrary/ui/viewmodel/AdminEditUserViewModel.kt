package com.example.smartlibrary.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.UpdateUserRequest
import com.example.smartlibrary.network.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class AdminEditUserViewModel(private val apiService: ApiService, private val userId: Int) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone = _phone.asStateFlow()

    private val _birthDate = MutableStateFlow("")
    val birthDate = _birthDate.asStateFlow()

    private val _avatarUrl = MutableStateFlow("")
    val avatarUrl = _avatarUrl.asStateFlow()

    private val _role = MutableStateFlow("USER")
    val role = _role.asStateFlow()

    private val _gender = MutableStateFlow("Khác")
    val gender = _gender.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _onUserUpdated = MutableStateFlow(false)
    val onUserUpdated = _onUserUpdated.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Sử dụng endpoint hiện có getUserProfile hoặc fetch từ danh sách admin
                val response = apiService.getUserProfile(userId.toString())
                _user.value = response
                _username.value = response.username ?: ""
                _email.value = response.email ?: ""
                _phone.value = response.phone ?: ""
                _birthDate.value = response.birthdate ?: ""
                _avatarUrl.value = response.avatar_url ?: ""
                _role.value = response.role ?: "USER"
                _gender.value = response.gender ?: "Khác"
            } catch (e: Exception) {
                _message.value = "Lỗi tải thông tin: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onUsernameChange(value: String) { _username.value = value }
    fun onEmailChange(value: String) { _email.value = value }
    fun onPhoneChange(value: String) { _phone.value = value }
    fun onBirthDateChange(value: String) { _birthDate.value = value }
    fun onAvatarUrlChange(value: String) { _avatarUrl.value = value }
    fun onRoleChange(value: String) { _role.value = value }
    fun onGenderChange(value: String) { _gender.value = value }

    fun updateUser() {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val request = UpdateUserRequest(
                    username = _username.value,
                    email = _email.value,
                    phone = _phone.value.ifBlank { null },
                    birthdate = _birthDate.value.ifBlank { null },
                    avatar_url = _avatarUrl.value.ifBlank { null },
                    role = _role.value,
                    gender = _gender.value
                )
                val response = apiService.updateAdminUser(userId, request)
                if (response.isSuccessful) {
                    _message.value = "Cập nhật thành công!"
                    _onUserUpdated.value = true
                } else {
                    _message.value = "Cập nhật thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val file = uriToFile(context, uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("files", file.name, requestFile)
                val response = apiService.uploadImage(listOf(body))
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    _avatarUrl.value = response.body()!![0]
                    _message.value = "Tải ảnh thành công"
                } else {
                    _message.value = "Lỗi tải ảnh"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi upload: ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_avatar_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }

    fun clearMessage() { _message.value = null }
}
