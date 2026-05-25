package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BorrowCardDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BorrowDetailViewModel(
    private val cardId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _borrowDetail = MutableStateFlow<BorrowCardDetailResponse?>(null)
    val borrowDetail = _borrowDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _onDeleted = MutableStateFlow(false)
    val onDeleted = _onDeleted.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getBorrowCardById(cardId.toString())
                _borrowDetail.value = response
            } catch (e: Exception) {
                _message.value = "Lỗi khi tải chi tiết: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.deleteBorrowCard(cardId.toString())
                if (response.isSuccessful) {
                    _message.value = "Xóa phiếu thành công"
                    _onDeleted.value = true
                } else {
                    _message.value = "Xóa phiếu thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
