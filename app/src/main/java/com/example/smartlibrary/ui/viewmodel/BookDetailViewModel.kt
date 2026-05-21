package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.SessionManager
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.network.BorrowRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookDetailViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
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

    private val userId: String
        get() = sessionManager.getUserId() ?: ""

    init {
        fetchBookDetails()
        if (userId.isNotEmpty()) {
            checkCartStatus()
        }
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

    fun checkCartStatus() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            try {
                val response = apiService.getCart(userId)
                val bookIdLong = bookId.toLongOrNull()
                val found = response.data?.any { it.bookId == bookIdLong } ?: false
                _isAddedToCart.value = found
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun borrowBook(onSuccess: () -> Unit) {
        if (userId.isEmpty()) {
            _message.value = "Bạn cần đăng nhập để mượn sách."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bookIdInt = bookId.toIntOrNull()
                if (bookIdInt == null) {
                    _message.value = "ID sách không hợp lệ."
                    return@launch
                }
                val response = apiService.borrowBook(BorrowRequest(userId = userId, bookIds = listOf(bookIdInt)))
                if (response.isSuccessful) {
                    _message.value = "Phiếu mượn đã được tạo thành công!"
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: ""
                    _message.value = if (errorMsg.contains("limit", true) || errorMsg.contains("tối đa", true)) {
                        "Bạn đã đạt giới hạn số lượng sách mượn tối đa!"
                    } else {
                        "Có lỗi xảy ra khi tạo phiếu mượn. Vui lòng kiểm tra lại."
                    }
                }
            } catch (e: Exception) {
                _message.value = "Không thể kết nối máy chủ để mượn sách."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToCart() {
        if (userId.isEmpty()) {
            _message.value = "Bạn cần đăng nhập để thêm vào giỏ hàng."
            return
        }

        if (_isAddedToCart.value) {
            _message.value = "Sách này đã có trong giỏ hàng của bạn rồi nhé!"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bookIdLong = bookId.toLongOrNull()
                if (bookIdLong == null) {
                    _message.value = "ID sách không hợp lệ."
                    return@launch
                }
                val response = apiService.addToCart(userId, listOf(bookIdLong))
                if (response.isSuccessful) {
                    _message.value = "Đã thêm sách vào giỏ thành công! 📚"
                    _isAddedToCart.value = true
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    if (response.code() == 400 || response.code() == 409 || 
                        errorBody.contains("already", true) || 
                        errorBody.contains("exists", true) || 
                        errorBody.contains("trùng", true)) {
                        _message.value = "Sách này đã có trong giỏ hàng của bạn rồi nhé!"
                        _isAddedToCart.value = true
                    } else {
                        _message.value = "Không thể thêm vào giỏ: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối máy chủ khi thêm vào giỏ hàng."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
