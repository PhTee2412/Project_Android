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

    // Bộ lọc
    private val _searchMode = MutableStateFlow("all")
    val searchMode: StateFlow<String> = _searchMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("all")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    // Phân trang
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val itemsPerPage = 10

    // Trạng thái xóa
    private val _deleteBook = MutableStateFlow<BookResponse?>(null)
    val deleteBook: StateFlow<BookResponse?> = _deleteBook.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allBooks = apiService.getAllBooks()
                android.util.Log.d("AdminBooksVM", "Số sách: ${allBooks.size}")
                _books.value = allBooks
            } catch (e: Exception) {
                android.util.Log.e("AdminBooksVM", "Lỗi tải sách", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchBooks() {
        val query = _searchQuery.value.trim()
        val mode = _searchMode.value
        if (mode == "all" && query.isEmpty()) {
            loadBooks()
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
                    year = if (mode == "year") query else null
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

    fun setSearchMode(mode: String) { _searchMode.value = mode }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setStatusFilter(status: String) {
        _statusFilter.value = status
        _currentPage.value = 1
    }
    fun setPage(page: Int) { _currentPage.value = page }

    // Xóa sách
    fun requestDelete(book: BookResponse) {
        _deleteBook.value = book
    }
    fun cancelDelete() {
        _deleteBook.value = null
    }
    fun confirmDelete() {
        val book = _deleteBook.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.deleteBook(book.maSach)
                if (response.isSuccessful) {
                    _books.value = _books.value.filter { it.maSach != book.maSach }
                    _message.value = "Xóa sách thành công"
                } else {
                    _message.value = "Xóa thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi xóa sách: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
                _deleteBook.value = null
            }
        }
    }

    // Lấy sách đã lọc và phân trang
    fun getPaginatedBooks(): List<BookResponse> {
        val filtered = when (_statusFilter.value) {
            "all" -> _books.value
            else -> _books.value.filter { it.trangThai == _statusFilter.value }
        }
        val total = filtered.size
        val from = (_currentPage.value - 1) * itemsPerPage
        val to = minOf(from + itemsPerPage, total)
        return if (from < total) filtered.subList(from, to) else emptyList()
    }

    fun getTotalPages(): Int {
        val filtered = when (_statusFilter.value) {
            "all" -> _books.value
            else -> _books.value.filter { it.trangThai == _statusFilter.value }
        }
        return if (filtered.isEmpty()) 1 else (filtered.size - 1) / itemsPerPage + 1
    }

    fun clearMessage() { _message.value = null }
}