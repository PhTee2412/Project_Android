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

    var showPassword by mutableStateOf(false)
        private set

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    fun onTabSelected(tab: String) {
        activeTab = tab
        resetForm()
    }

    private fun resetForm() {
        identifier = ""
        password = ""
        username = ""
        birthdate = ""
        gender = "Nam"
        otp = ""
        _isOtpSent.value = false
        _message.value = null
    }

    fun togglePasswordVisibility() {
        showPassword = !showPassword
    }

    fun clearMessage() {
        _message.value = null
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
            _message.value = "Vui lòng điền các trường bắt buộc"
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
                val response = apiService.register(request)
                if (response.message?.contains("thành công") == true || response.status == "success") {
                    if (type == "email") {
                        _isOtpSent.value = true
                        _message.value = "Mã OTP đã gửi đến email của bạn"
                    } else {
                        activeTab = "login"
                        _message.value = "Đăng ký thành công. Vui lòng đăng nhập."
                    }
                } else {
                    _message.value = response.message ?: "Đăng ký thất bại"
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _message.value = errorBody ?: "Lỗi đăng ký"
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.localizedMessage}"
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
                val (_, value) = determineInputType(identifier)
                val response = apiService.verifyOtp(VerifyOtpRequest(email = value, otp = otp))
                if (response.status == "success") {
                    _isOtpSent.value = false
                    activeTab = "login"
                    _message.value = "Xác thực thành công. Vui lòng đăng nhập."
                } else {
                    _message.value = response.message ?: "Xác thực thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi xác thực: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Mock Google/Facebook login – sau này thay bằng code thật
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