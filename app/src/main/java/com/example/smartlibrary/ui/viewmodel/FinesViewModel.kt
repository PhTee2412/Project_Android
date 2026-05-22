package com.example.smartlibrary.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.SessionManager
import com.example.smartlibrary.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FinesViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _fines = MutableStateFlow<List<FineResponse>>(emptyList())
    val fines: StateFlow<List<FineResponse>> = _fines.asStateFlow()

    private val _selectedTab = MutableStateFlow("CHUA_THANH_TOAN")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _filteredFines = MutableStateFlow<List<FineResponse>>(emptyList())
    val filteredFines: StateFlow<List<FineResponse>> = _filteredFines.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _selectedFine = MutableStateFlow<FineDetailResponse?>(null)
    val selectedFine: StateFlow<FineDetailResponse?> = _selectedFine.asStateFlow()

    private val userId: String
        get() = sessionManager.getUserId() ?: ""

    init {
        loadFines()
    }

    fun loadFines() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getFinesByUser(userId)
                _fines.value = response
                filterFines()
            } catch (e: Exception) {
                _message.value = "Lỗi khi tải phiếu phạt: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onTabSelected(tab: String) {
        _selectedTab.value = tab
        filterFines()
    }

    private fun filterFines() {
        _filteredFines.value = _fines.value.filter { it.trangThai == _selectedTab.value }
    }

    fun loadFineDetail(fineId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getFineById(fineId)
                _selectedFine.value = response
            } catch (e: Exception) {
                _message.value = "Không tìm thấy chi tiết phiếu phạt: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun payFine(context: Context, fineId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.payFineByMomo(fineId)
                if (response.payUrl.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(response.payUrl))
                    context.startActivity(intent)
                } else {
                    _message.value = "Không nhận được link thanh toán từ Momo"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi thanh toán: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}