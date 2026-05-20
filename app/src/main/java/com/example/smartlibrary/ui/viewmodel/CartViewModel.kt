package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.SessionManager
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.CartItemDTO
import com.example.smartlibrary.network.BorrowRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val onCartCountChanged: (Int) -> Unit
) : ViewModel() {

    private val _cartBooks = MutableStateFlow<List<CartItemDTO>>(emptyList())
    val cartBooks: StateFlow<List<CartItemDTO>> = _cartBooks.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())  // Long
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _maxBorrowedBooks = MutableStateFlow(5)
    val maxBorrowedBooks: StateFlow<Int> = _maxBorrowedBooks.asStateFlow()

    private val userId: String
        get() = sessionManager.getUserId() ?: ""

    init {
        loadCart()
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val setting = apiService.getSettings()
                _maxBorrowedBooks.value = setting.maxBorrowedBooks ?: 5
            } catch (e: Exception) {
                _maxBorrowedBooks.value = 5
            }
        }
    }

    fun loadCart() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getCart(userId)
                val books = response.data ?: emptyList()
                _cartBooks.value = books
                onCartCountChanged(books.size)
                val validIds = books.map { it.bookId }.toSet()
                _selectedIds.value = _selectedIds.value.intersect(validIds)
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSelection(bookId: Long) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply {
            if (contains(bookId)) remove(bookId) else add(bookId)
        }
    }

    fun selectAll() {
        _selectedIds.value = _cartBooks.value.map { it.bookId }.toSet()
    }

    fun deselectAll() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        val idsToDelete = _selectedIds.value.toList()
        if (idsToDelete.isEmpty() || userId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.removeBooksFromCart(userId, idsToDelete)
                if (response.isSuccessful) {
                    _message.value = "Đã xóa sách khỏi giỏ hàng"
                    loadCart()
                } else {
                    _message.value = "Không thể xóa sách: ${response.code()}"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun borrowSelected() {
        val idsToBorrow = _selectedIds.value.toList()
        if (idsToBorrow.isEmpty() || userId.isEmpty()) return

        if (idsToBorrow.size > _maxBorrowedBooks.value) {
            _message.value = "Bạn chỉ được phép mượn tối đa ${_maxBorrowedBooks.value} cuốn sách!"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Chuyển Long sang Int cho BorrowRequest nếu backend yêu cầu Int
                val intIds = idsToBorrow.map { it.toInt() }
                val response = apiService.borrowBook(
                    BorrowRequest(userId = userId, bookIds = intIds)
                )
                if (response.isSuccessful) {
                    // Sau khi mượn thành công, xóa sách khỏi giỏ
                    apiService.removeBooksFromCart(userId, idsToBorrow)
                    _message.value = "Đã gửi yêu cầu mượn sách thành công!"
                    loadCart()
                    _selectedIds.value = emptySet()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Lỗi không xác định"
                    _message.value = "Yêu cầu mượn thất bại: $errorMsg"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi kết nối: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}