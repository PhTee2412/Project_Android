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
                // Nếu đang bật sort, ta dùng searchBooksAdmin với các field null để lấy toàn bộ sách đã sort
                if (_sortByBorrowCount.value) {
                    val results = apiService.searchBooksAdmin(sortByBorrowCount = true)
                    _books.value = results
                } else {
                    val allBooks = apiService.getAllBooks()
                    _books.value = allBooks
                }
                _totalPages.value = if (_books.value.isEmpty()) 1 else (_books.value.size - 1) / booksPerPage + 1
            } catch (e: Exception) {
                _message.value = "Lỗi tải sách: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchBooks() {
        val query = _searchQuery.value.trim()
        // Nếu không có query và không bật sort, quay về loadAllBooks cơ bản
        if (query.isEmpty() && _searchMode.value == "all" && !_sortByBorrowCount.value) {
            loadAllBooks()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = apiService.searchBooksAdmin(
                    all = if (_searchMode.value == "all" && query.isNotEmpty()) query else null,
                    title = if (_searchMode.value == "title") query else null,
                    author = if (_searchMode.value == "author") query else null,
                    category = if (_searchMode.value == "category") query else null,
                    publisher = if (_searchMode.value == "publisher") query else null,
                    year = if (_searchMode.value == "year") query else null,
                    sortByBorrowCount = _sortByBorrowCount.value
                )
                _books.value = results
                _totalPages.value = if (results.isEmpty()) 1 else (results.size - 1) / booksPerPage + 1
                _currentPage.value = 1
            } catch (e: Exception) {
                _message.value = "Lỗi tìm kiếm: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setActiveSection(section: String) { _activeSection.value = section }
    fun setPage(page: Int) { if (page in 1.._totalPages.value) _currentPage.value = page }
    fun setSearchMode(mode: String) { _searchMode.value = mode }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    
    fun setSortByBorrowCount(sort: Boolean) { 
        _sortByBorrowCount.value = sort 
        // Khi thay đổi sort, tự động reload lại danh sách theo tiêu chí mới
        searchBooks()
    }

    fun clearMessage() { _message.value = null }

    fun getPaginatedBooks(): List<BookResponse> {
        val all = _books.value
        val from = (_currentPage.value - 1) * booksPerPage
        val to = minOf(from + booksPerPage, all.size)
        return if (from < all.size) all.subList(from, to) else emptyList()
    }
}