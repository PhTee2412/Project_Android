package com.example.smartlibrary.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.SessionManager
import com.example.smartlibrary.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class AuthViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val onLoginSuccess: () -> Unit = {}
) : ViewModel() {

    var activeTab by mutableStateOf("login")
        private set

    var identifier by mutableStateOf("") // Email hoặc Số điện thoại
    var password by mutableStateOf("")
    var username by mutableStateOf("")
    var birthdate by mutableStateOf("")
    var gender by mutableStateOf("Nam")
    var otp by mutableStateOf("")

    // Forgot Password States
    var forgotPasswordStep by mutableStateOf("none") // "none", "request", "reset"
    var newPassword by mutableStateOf("")
    var resendTimeout by mutableStateOf(0)
    private var timerJob: Job? = null

    var showPassword by mutableStateOf(false)
        private set

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Sử dụng StateFlow để đồng bộ trạng thái OTP cho UI
    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    fun onTabSelected(tab: String) {
        activeTab = tab
        forgotPasswordStep = "none"
        resetForm()
    }

    private fun resetForm() {
        identifier = ""
        password = ""
        username = ""
        birthdate = ""
        gender = "Nam"
        otp = ""
        newPassword = ""
        _isOtpSent.value = false
        _message.value = null
        stopResendTimer()
    }

    fun togglePasswordVisibility() {
        showPassword = !showPassword
    }

    fun clearMessage() {
        _message.value = null
    }

    fun cancelOtp() {
        _isOtpSent.value = false
        otp = ""
    }

    private fun determineInputType(input: String): Pair<String, String> {
        return if (input.contains("@")) "email" to input else "phone" to input
    }

    fun showMessage(msg: String) {
        _message.value = msg
    }

    fun login() {
        if (identifier.isBlank() || password.isBlank()) {
            _message.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (type, value) = determineInputType(identifier)
                val request = LoginRequest(
                    email = if (type == "email") value else null,
                    phone = if (type == "phone") value else null,
                    password = password
                )
                val response = apiService.login(request)
                if (response.data?.accessToken != null) {
                    val user = response.data.user
                    sessionManager.saveSession(
                        userId = user?.id?.toString() ?: "",
                        token = response.data.accessToken,
                        name = user?.name,
                        email = user?.email
                    )
                    _message.value = response.message ?: "Đăng nhập thành công"
                    onLoginSuccess()
                } else {
                    _message.value = response.message ?: "Đăng nhập thất bại"
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _message.value = errorBody ?: "Sai tài khoản hoặc mật khẩu"
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register() {
        if (username.isBlank() || identifier.isBlank() || password.isBlank()) {
            _message.value = "Vui lòng điền đầy đủ thông tin đăng ký"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (type, value) = determineInputType(identifier)
                val request = RegisterRequest(
                    username = username,
                    email = if (type == "email") value else null,
                    phone = if (type == "phone") value else null,
                    password = password,
                    birthdate = if (birthdate.isNotBlank()) birthdate else null,
                    gender = gender
                )

                apiService.register(request)

                _isOtpSent.value = true
                _message.value = "Mã OTP đã được gửi thành công!"
                Log.d("AuthViewModel", "OTP screen ON")
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _message.value = errorBody ?: "Tài khoản hoặc email này đã tồn tại"
            } catch (e: Exception) {
                _message.value = "Lỗi hệ thống: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp() {
        if (otp.length != 6) {
            _message.value = "Vui lòng nhập mã OTP gồm 6 chữ số"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (type, value) = determineInputType(identifier)

                val response = apiService.verifyOtp(
                    VerifyOtpRequest(
                        email = if (type == "email") value else null,
                        phone = if (type == "phone") value else null,
                        otp = otp
                    )
                )

                if (response.status?.equals("success", ignoreCase = true) == true) {
                    _isOtpSent.value = false
                    activeTab = "login"
                    otp = ""
                    _message.value = "Xác thực thành công! Mời bạn đăng nhập."
                } else {
                    _message.value = response.message ?: "Mã OTP không đúng hoặc đã hết hạn"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi xác thực: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun requestForgotPassword() {
        if (identifier.isBlank()) {
            _message.value = "Vui lòng nhập Email hoặc Số điện thoại"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.forgotPassword(ForgotPasswordRequest(identifier))
                if (response.isSuccessful) {
                    _message.value = "Mã OTP khôi phục mật khẩu đã được gửi"
                    forgotPasswordStep = "reset"
                    startResendTimer()
                } else {
                    _message.value = "Gửi OTP không thành công"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetForgotPassword() {
        if (otp.isBlank() || newPassword.isBlank()) {
            _message.value = "Vui lòng nhập mã OTP và mật khẩu mới"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.resetPassword(
                    ResetPasswordRequest(identifier, otp, newPassword)
                )
                if (response.isSuccessful) {
                    _message.value = "Đổi mật khẩu thành công!"
                    forgotPasswordStep = "none"
                    activeTab = "login"
                    resetForm()
                } else {
                    _message.value = "Cập nhật mật khẩu thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        resendTimeout = 60
        timerJob = viewModelScope.launch {
            while (resendTimeout > 0) {
                delay(1000)
                resendTimeout--
            }
        }
    }

    private fun stopResendTimer() {
        timerJob?.cancel()
        resendTimeout = 0
    }

    fun loginWithGoogle(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.loginWithGoogle(SocialLoginRequest(token))
                if (response.data?.accessToken != null) {
                    val user = response.data.user
                    sessionManager.saveSession(
                        userId = user?.id?.toString() ?: "",
                        token = response.data.accessToken,
                        name = user?.name,
                        email = user?.email
                    )
                    _message.value = response.message ?: "Đăng nhập Google thành công"
                    onLoginSuccess()
                } else {
                    _message.value = response.message ?: "Đăng nhập Google thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối Google: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginWithFacebook(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.loginWithFacebook(SocialLoginRequest(token))
                if (response.data?.accessToken != null) {
                    val user = response.data.user
                    sessionManager.saveSession(
                        userId = user?.id?.toString() ?: "",
                        token = response.data.accessToken,
                        name = user?.name,
                        email = user?.email
                    )
                    _message.value = response.message ?: "Đăng nhập Facebook thành công"
                    onLoginSuccess()
                } else {
                    _message.value = response.message ?: "Đăng nhập Facebook thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối Facebook: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
