package com.example.smartlibrary.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.AddUserRequest
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.VerifyOtpRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class AddUserStep { FORM, OTP }

class AdminAddUserViewModel(private val apiService: ApiService) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone = _phone.asStateFlow()

    private val _birthDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    val birthDate = _birthDate.asStateFlow()

    private val _avatarUrl = MutableStateFlow("")
    val avatarUrl = _avatarUrl.asStateFlow()

    private val _role = MutableStateFlow("USER")
    val role = _role.asStateFlow()

    private val _gender = MutableStateFlow("Khác")
    val gender = _gender.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _step = MutableStateFlow(AddUserStep.FORM)
    val step = _step.asStateFlow()

    private val _otp = MutableStateFlow("")
    val otp = _otp.asStateFlow()

    private val _tempEmail = MutableStateFlow("")
    val tempEmail = _tempEmail.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())
    val errors = _errors.asStateFlow()

    private val _onUserCreated = MutableStateFlow(false)
    val onUserCreated = _onUserCreated.asStateFlow()

    fun onUsernameChange(value: String) { _username.value = value }
    fun onEmailChange(value: String) { _email.value = value }
    fun onPhoneChange(value: String) { _phone.value = value }
    fun onBirthDateChange(value: String) { _birthDate.value = value }
    fun onAvatarUrlChange(value: String) { _avatarUrl.value = value }
    fun onRoleChange(value: String) { _role.value = value }
    fun onGenderChange(value: String) { _gender.value = value }
    fun onOtpChange(value: String) { _otp.value = value }

    fun setStep(step: AddUserStep) { _step.value = step }

    private fun validate(): Boolean {
        val errs = mutableMapOf<String, String>()
        if (_username.value.isBlank()) errs["username"] = "Tên người dùng không được để trống"
        if (_email.value.isBlank()) {
            errs["email"] = "Email không được để trống"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value.trim()).matches()) {
            errs["email"] = "Email không đúng định dạng"
        }
        
        val phoneRegex = Regex("(84|0[3|5|7|8|9])+([0-9]{8})\\b")
        if (_phone.value.isNotBlank() && !phoneRegex.matches(_phone.value.trim())) {
            errs["phone"] = "Số điện thoại không hợp lệ"
        }
        
        _errors.value = errs
        return errs.isEmpty()
    }

    fun createUser() {
        if (!validate()) return

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val request = AddUserRequest(
                    username = _username.value.trim(),
                    email = _email.value.trim(),
                    phone = _phone.value.trim().ifBlank { "" },
                    birthdate = _birthDate.value,
                    avatar_url = _avatarUrl.value.ifBlank { "" },
                    role = _role.value,
                    gender = _gender.value
                )
                val response = apiService.addAdminUser(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    val msg = body?.message ?: ""
                    
                    if (msg.contains("otp", ignoreCase = true)) {
                        _tempEmail.value = _email.value.trim()
                        _step.value = AddUserStep.OTP
                        _message.value = "Đã gửi OTP xác thực đến email!"
                    } else {
                        _message.value = "Tạo người dùng thành công!"
                        _onUserCreated.value = true
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = try {
                        val json = Gson().fromJson(errorBody, JsonObject::class.java)
                        json.get("message")?.asString ?: "Tạo người dùng thất bại"
                    } catch (e: Exception) {
                        "Tạo người dùng thất bại"
                    }
                    _message.value = errorMsg
                }
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun verifyOtp() {
        if (_otp.value.length < 6) {
            _message.value = "Vui lòng nhập mã OTP đầy đủ"
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val request = VerifyOtpRequest(email = _tempEmail.value, otp = _otp.value)
                val response = apiService.verifyOtpCreate(request)
                if (response.isSuccessful) {
                    _message.value = "Xác thực thành công!"
                    _onUserCreated.value = true
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = try {
                        val json = Gson().fromJson(errorBody, JsonObject::class.java)
                        json.get("message")?.asString ?: "Mã OTP không đúng"
                    } catch (e: Exception) {
                        "Mã OTP không đúng"
                    }
                    _message.value = errorMsg
                }
            } catch (e: Exception) {
                _message.value = "Lỗi xác thực: ${e.message}"
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
