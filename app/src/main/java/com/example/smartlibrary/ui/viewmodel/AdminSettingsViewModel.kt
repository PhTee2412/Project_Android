package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.Setting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminSettingsViewModel(private val apiService: ApiService) : ViewModel() {

    private val _finePerDay = MutableStateFlow(0)
    val finePerDay = _finePerDay.asStateFlow()

    private val _waitingToTake = MutableStateFlow(0)
    val waitingToTake = _waitingToTake.asStateFlow()

    private val _borrowDay = MutableStateFlow(0)
    val borrowDay = _borrowDay.asStateFlow()

    private val _startToMail = MutableStateFlow(0)
    val startToMail = _startToMail.asStateFlow()

    private val _maxBorrowedBooks = MutableStateFlow(0)
    val maxBorrowedBooks = _maxBorrowedBooks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private var currentSettingId: Long? = null

    init {
        loadSettings()
    }

    fun onFinePerDayChange(value: String) {
        _finePerDay.value = value.toIntOrNull() ?: 0
    }

    fun onWaitingToTakeChange(value: String) {
        _waitingToTake.value = value.toIntOrNull() ?: 0
    }

    fun onBorrowDayChange(value: String) {
        _borrowDay.value = value.toIntOrNull() ?: 0
    }

    fun onStartToMailChange(value: String) {
        _startToMail.value = value.toIntOrNull() ?: 0
    }

    fun onMaxBorrowedBooksChange(value: String) {
        _maxBorrowedBooks.value = value.toIntOrNull() ?: 0
    }

    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val settings = apiService.getSettings()
                currentSettingId = settings.id
                _finePerDay.value = settings.finePerDay
                _waitingToTake.value = settings.waitingToTake
                _borrowDay.value = settings.borrowDay
                _startToMail.value = settings.startToMail
                _maxBorrowedBooks.value = settings.maxBorrowedBooks ?: 0
            } catch (e: Exception) {
                _message.value = "Lỗi khi tải cài đặt: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val settings = Setting(
                    id = currentSettingId,
                    finePerDay = _finePerDay.value,
                    waitingToTake = _waitingToTake.value,
                    borrowDay = _borrowDay.value,
                    startToMail = _startToMail.value,
                    maxBorrowedBooks = _maxBorrowedBooks.value
                )
                val response = apiService.updateSettings(settings)
                if (response.isSuccessful) {
                    _message.value = "Lưu cài đặt thành công"
                    loadSettings()
                } else {
                    _message.value = "Lỗi khi lưu cài đặt: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
