package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.network.DashboardResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _dashboardData = MutableStateFlow<DashboardResponse?>(null)
    val dashboardData: StateFlow<DashboardResponse?> = _dashboardData.asStateFlow()

    // Cache toàn bộ sách (dùng chung cho danh sách và tìm kiếm)
    private val _allBooks = MutableStateFlow<List<BookResponse>>(emptyList())
    val allBooks: StateFlow<List<BookResponse>> = _allBooks.asStateFlow()

    // Danh sách hiển thị (đã lọc/tìm kiếm)
    private val _books = MutableStateFlow<List<BookResponse>>(emptyList())
    val books: StateFlow<List<BookResponse>> = _books.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _activeSection = MutableStateFlow("danhSach")
    val activeSection: StateFlow<String> = _activeSection.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _searchMode = MutableStateFlow("all")
    val searchMode: StateFlow<String> = _searchMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortByBorrowCount = MutableStateFlow(false)
    val sortByBorrowCount: StateFlow<Boolean> = _sortByBorrowCount.asStateFlow()

    private val booksPerPage = 10

    init {
        loadDashboard()
        loadAllBooks()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _dashboardData.value = apiService.getDashboard()
            } catch (e: Exception) {
                _message.value = "Lỗi tải dashboard: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Nếu cache đã có, không gọi lại API (tiết kiệm thời gian)
                if (_allBooks.value.isNotEmpty()) {
                    _books.value = _allBooks.value
                    updateTotalPages()
                    return@launch
                }
                val all = apiService.getAllBooksAdmin()
                _allBooks.value = all
                _books.value = all
                updateTotalPages()
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
        val all = _allBooks.value

        // Nếu cache rỗng, tải trước
        if (all.isEmpty()) {
            loadAllBooks()
            return
        }

        val filtered = if (query.isEmpty() && mode == "all") {
            all
        } else {
            all.filter { book ->
                when (mode) {
                    "all" -> book.tenSach.contains(query, ignoreCase = true) ||
                            book.tenTacGia?.contains(query, ignoreCase = true) == true ||
                            book.nxb?.contains(query, ignoreCase = true) == true ||
                            book.categoryChildName?.contains(query, ignoreCase = true) == true
                    "title" -> book.tenSach.contains(query, ignoreCase = true)
                    "author" -> book.tenTacGia?.contains(query, ignoreCase = true) == true
                    "category" -> book.categoryChildName?.contains(query, ignoreCase = true) == true
                    "publisher" -> book.nxb?.contains(query, ignoreCase = true) == true
                    "year" -> book.nam?.toString()?.contains(query) == true
                    else -> true
                }
            }
        }

        val sorted = if (_sortByBorrowCount.value) {
            filtered.sortedByDescending { it.soLuongMuon }
        } else {
            filtered
        }

        _books.value = sorted
        updateTotalPages()
        _currentPage.value = 1
    }

    private fun updateTotalPages() {
        _totalPages.value = if (_books.value.isEmpty()) 1 else (_books.value.size - 1) / booksPerPage + 1
    }

    fun setActiveSection(section: String) { _activeSection.value = section }
    fun setPage(page: Int) { if (page in 1.._totalPages.value) _currentPage.value = page }
    fun setSearchMode(mode: String) { _searchMode.value = mode }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun setSortByBorrowCount(sort: Boolean) {
        _sortByBorrowCount.value = sort
        searchBooks() // Tự động sắp xếp lại (client‑side)
    }

    fun clearMessage() { _message.value = null }

    fun getPaginatedBooks(): List<BookResponse> {
        val all = _books.value
        val from = (_currentPage.value - 1) * booksPerPage
        val to = minOf(from + booksPerPage, all.size)
        return if (from < all.size) all.subList(from, to) else emptyList()
    }
}