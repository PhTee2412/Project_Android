package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.network.BorrowRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookDetailViewModel(
    private val apiService: ApiService,
    private val bookId: String
) : ViewModel() {

    private val _book = MutableStateFlow<BookResponse?>(null)
    val book: StateFlow<BookResponse?> = _book.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isAddedToCart = MutableStateFlow(false)
    val isAddedToCart: StateFlow<Boolean> = _isAddedToCart.asStateFlow()

    init {
        fetchBookDetails()
    }

    private fun fetchBookDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.getBookById(bookId)
                _book.value = response
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Không thể tải thông tin sách. Vui lòng thử lại sau."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkCartStatus(userId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getCart(userId)
                val found = response.data?.any { it.maSach == bookId } ?: false
                _isAddedToCart.value = found
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun borrowBook(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val bookIdInt = bookId.toIntOrNull()
                if (bookIdInt == null) {
                    _message.value = "ID sách không hợp lệ."
                    return@launch
                }
                // Next.js: { userId, bookIds: [parseInt(id)] }
                val response = apiService.borrowBook(BorrowRequest(userId = userId, bookIds = listOf(bookIdInt)))
                if (response.isSuccessful) {
                    _message.value = "Phiếu mượn đã được tạo thành công!"
                    onSuccess()
                } else {
                    _message.value = "Có lỗi xảy ra khi tạo phiếu mượn"
                }
            } catch (e: Exception) {
                _message.value = "Không thể mượn sách. Vui lòng thử lại."
            }
        }
    }

    fun addToCart(userId: String) {
        viewModelScope.launch {
            try {
                // Next.js: POST /api/cart/{userId}/add/books body [id]
                val response = apiService.addToCart(userId, listOf(bookId))
                if (response.isSuccessful) {
                    _message.value = "Đã thêm sách vào giỏ!"
                    _isAddedToCart.value = true
                } else {
                    _message.value = "Có lỗi xảy ra khi thêm sách vào giỏ."
                }
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối: ${e.localizedMessage}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
