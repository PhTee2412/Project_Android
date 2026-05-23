package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminBooksViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _books = MutableStateFlow<List<BookResponse>>(emptyList())
    val books: StateFlow<List<BookResponse>> = _books.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _searchMode = MutableStateFlow("all")
    val searchMode: StateFlow<String> = _searchMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("all")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _deleteBook = MutableStateFlow<BookResponse?>(null)
    val deleteBook: StateFlow<BookResponse?> = _deleteBook.asStateFlow()

    private var hasLoaded = false

    fun loadBooksIfNeeded() {
        if (hasLoaded && _books.value.isNotEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = apiService.getAllBooksAdmin()
                logBooksStatus(result)
                _books.value = result
                hasLoaded = true
            } catch (e: Exception) {
                _message.value = "Lỗi tải sách: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchBooks() {
        val query = _searchQuery.value.trim()
        val mode = _searchMode.value
        if (query.isEmpty() && mode == "all") {
            // Nếu không có từ khoá, chỉ cần load tất cả nếu chưa có
            loadBooksIfNeeded()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = apiService.searchBooksAdmin(
                    all = if (mode == "all") query else null,
                    title = if (mode == "title") query else null,
                    author = if (mode == "author") query else null,
                    category = if (mode == "category") query else null,
                    publisher = if (mode == "publisher") query else null,
                    year = if (mode == "year") query else null,
                    status = null, // tìm kiếm thường không lọc status
                    sortByBorrowCount = false
                )
                _books.value = results
                _currentPage.value = 1
            } catch (e: Exception) {
                _message.value = "Lỗi tìm kiếm: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun logBooksStatus(list: List<BookResponse>) { /* giữ nguyên */ }

    fun setSearchMode(mode: String) { _searchMode.value = mode }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setStatusFilter(status: String) {
        _statusFilter.value = status
        _currentPage.value = 1
    }
    fun setPage(page: Int) { _currentPage.value = page }

    fun requestDelete(book: BookResponse) { _deleteBook.value = book }
    fun cancelDelete() { _deleteBook.value = null }
    fun confirmDelete() {
        val book = _deleteBook.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.deleteBook(book.maSach)
                if (response.isSuccessful) {
                    // Cập nhật trạng thái cục bộ thay vì xóa khỏi list
                    _books.value = _books.value.map {
                        if (it.maSach == book.maSach) it.copy(trangThai = "DA_XOA") else it
                    }
                    _message.value = "Xoá sách thành công"
                } else {
                    _message.value = "Xoá thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi xoá sách: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
                _deleteBook.value = null
            }
        }
    }

    fun clearMessage() { _message.value = null }
}
