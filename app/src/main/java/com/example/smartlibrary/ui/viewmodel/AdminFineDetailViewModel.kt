package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.FineDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminFineDetailViewModel(
    private val apiService: ApiService,
    private val fineId: String
) : ViewModel() {
    private val _fine = MutableStateFlow<FineDetailResponse?>(null)
    val fine = _fine.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    init {
        loadFine()
    }

    fun loadFine() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _fine.value = apiService.getFineById(fineId)
            } catch (e: Exception) {
                _message.value = "Lỗi tải chi tiết: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun payFine() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.payFine(fineId)
                if (response.isSuccessful) {
                    _message.value = "Xác nhận thanh toán thành công!"
                    loadFine() // Reload to update status
                } else {
                    _message.value = "Thanh toán thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() { _message.value = null }
}
