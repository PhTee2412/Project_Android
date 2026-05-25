package com.example.smartlibrary.ui.viewmodel

import android.util.Log
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
                Log.d("FineDetail", "Pay fine response: code=${response.code()}, isSuccessful=${response.isSuccessful}")
                // Dù server trả về gì, ta vẫn tải lại để cập nhật trạng thái thực tế
                loadFine()
                // Hiển thị thông báo phù hợp
                _message.value = if (response.isSuccessful) {
                    "Xác nhận thanh toán thành công!"
                } else {
                    "Yêu cầu đã được gửi, vui lòng kiểm tra lại trạng thái"
                }
            } catch (e: Exception) {
                Log.e("FineDetail", "Pay fine exception", e)
                loadFine()
                _message.value = "Đã gửi yêu cầu, đang cập nhật..."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() { _message.value = null }
}