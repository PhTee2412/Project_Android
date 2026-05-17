package com.example.smartlibrary.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FinesViewModel(private val apiService: ApiService) : ViewModel() {

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

    // Sử dụng userId consistent với BorrowedCardsViewModel
    private val userId = "2"

    init {
        loadFines()
    }

    fun loadFines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getFinesByUser(userId)
                if (response.isEmpty()) {
                    _fines.value = getMockFines()
                } else {
                    _fines.value = response
                }
            } catch (e: Exception) {
                _fines.value = getMockFines()
                _message.value = "Lỗi khi tải phiếu phạt, đang hiển thị dữ liệu mẫu."
            } finally {
                filterFines()
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
                // Hỗ trợ mock data cho cả ID 1 và 2
                if (fineId == "1" || fineId == "2") {
                    _selectedFine.value = getMockFineDetail(fineId)
                } else {
                    _message.value = "Không tìm thấy chi tiết phiếu phạt."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun payFine(context: Context, fineId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.payFineByMomo(fineId)
                if (response.payUrl.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(response.payUrl))
                    context.startActivity(intent)
                } else {
                    _message.value = "Không nhận được link thanh toán."
                }
            } catch (e: Exception) {
                _message.value = "Đang chuyển đến cổng thanh toán Momo (MOCK)..."
                // Sử dụng URL tạo QR code mock để người dùng thấy QR thay vì chỉ trang chủ Momo
                val mockPayUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=MomoPayment_Fine_$fineId"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mockPayUrl))
                context.startActivity(intent)
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun getMockFines(): List<FineResponse> = listOf(
        FineResponse(
            id = 1,
            userId = UserBrief(1, "Nguyễn Văn A"),
            soTien = 50000.0,
            noiDung = "Trả sách trễ hạn",
            trangThai = "CHUA_THANH_TOAN",
            ngayThanhToan = null
        ),
        FineResponse(
            id = 2,
            userId = UserBrief(1, "Nguyễn Văn A"),
            soTien = 200000.0,
            noiDung = "Làm mất sách",
            trangThai = "DA_THANH_TOAN",
            ngayThanhToan = "2024-12-01"
        )
    )

    private fun getMockFineDetail(fineId: String): FineDetailResponse {
        val isPaid = fineId == "2"
        return FineDetailResponse(
            id = fineId.toIntOrNull() ?: 1,
            userId = UserBrief(1, "Nguyễn Văn A"),
            soTien = if (isPaid) 200000.0 else 50000.0,
            noiDung = if (isPaid) "Làm mất sách" else "Trả sách trễ hạn",
            trangThai = if (isPaid) "DA_THANH_TOAN" else "CHUA_THANH_TOAN",
            ngayThanhToan = if (isPaid) "2024-12-01" else null,
            tenND = "Nguyễn Văn A",
            cardId = BorrowCardInFine(
                id = if (isPaid) 11 else 10,
                borrowedBooks = listOf(
                    BorrowedBookBrief(
                        bookId = if (isPaid) 42 else 37,
                        childBookId = if (isPaid) "42b" else "37a",
                        name = if (isPaid) "Lập Trình Android" else "Đồi Gió Hú",
                        author = if (isPaid) "Nguyễn Huy" else "Emily Brontë",
                        image = if (isPaid) "https://example.com/android.jpg" else "https://res.cloudinary.com/dlit96as6/image/upload/v1731671813/doigiohu_qcjzns.jpg",
                        category = if (isPaid) "Kỹ Thuật" else "Truyện Ngắn",
                        publisher = "NXB Trẻ"
                    )
                )
            )
        )
    }
}
