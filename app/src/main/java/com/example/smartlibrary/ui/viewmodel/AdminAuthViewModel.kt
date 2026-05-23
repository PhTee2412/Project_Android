package com.example.smartlibrary.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.SessionManagerAdmin
import com.example.smartlibrary.network.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminAuthViewModel(
    private val apiService: ApiService,
    private val sessionManagerAdmin: SessionManagerAdmin,
    private val onLoginSuccess: () -> Unit = {}
) : ViewModel() {

    var identifier by mutableStateOf("")
    var password by mutableStateOf("")
    var otp by mutableStateOf("")

    var showPassword by mutableStateOf(false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    private val _otpEmail = MutableStateFlow("")
    val otpEmail: StateFlow<String> = _otpEmail.asStateFlow()

    private val _resendCountdown = MutableStateFlow(0)
    val resendCountdown: StateFlow<Int> = _resendCountdown.asStateFlow()

    fun togglePasswordVisibility() {
        showPassword = !showPassword
    }

    fun clearMessage() {
        _message.value = null
    }

    fun login() {
        if (identifier.isBlank() || password.isBlank()) {
            _message.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = AdminLoginRequest(
                    email = if (identifier.contains("@")) identifier else null,
                    phone = if (!identifier.contains("@")) identifier else null,
                    password = password
                )
                val response = apiService.adminLogin(request)

                if (response.data?.accessToken != null) {
                    // Trường hợp đặc biệt: server trả thẳng token (không OTP)
                    val user = response.data.user
                    if (user?.role.equals("ADMIN", ignoreCase = true)) {
                        sessionManagerAdmin.saveSession(
                            userId = user?.id.toString(),
                            token = response.data.accessToken,
                            name = user?.name,
                            email = user?.email
                        )
                        sessionManagerAdmin.saveUserRole("ADMIN")
                        _message.value = response.message ?: "Đăng nhập admin thành công"
                        onLoginSuccess()
                    } else {
                        _message.value = "Tài khoản này không có quyền admin"
                    }
                } else if (response.email != null) {
                    // Backend yêu cầu OTP
                    _otpEmail.value = response.email
                    _isOtpSent.value = true
                    _message.value = response.message ?: "Mã OTP đã được gửi đến email của bạn"
                    startResendCountdown()
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

    fun verifyOtp() {
        if (otp.length != 6) {
            _message.value = "Mã OTP phải có 6 chữ số"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val email = _otpEmail.value
                val response = apiService.verifyAdminOtp(VerifyOtpRequest(email = email, otp = otp))
                if (response.data?.accessToken != null) {
                    val user = response.data.user
                    sessionManagerAdmin.saveSession(
                        userId = user?.id.toString(),
                        token = response.data.accessToken,
                        name = user?.name,
                        email = user?.email
                    )
                    sessionManagerAdmin.saveUserRole("ADMIN")
                    _isOtpSent.value = false
                    _message.value = response.message ?: "Đăng nhập admin thành công"
                    onLoginSuccess()
                } else {
                    _message.value = response.message ?: "Mã OTP không đúng hoặc đã hết hạn"
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _message.value = errorBody ?: "OTP không hợp lệ"
            } catch (e: Exception) {
                _message.value = "Lỗi xác thực OTP: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resendOtp() {
        if (_resendCountdown.value > 0) return
        login()
    }

    private fun startResendCountdown() {
        viewModelScope.launch {
            _resendCountdown.value = 60
            while (_resendCountdown.value > 0) {
                delay(1000)
                _resendCountdown.value -= 1
            }
        }
    }
}