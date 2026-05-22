package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.SessionManager
import com.example.smartlibrary.data.model.Book
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.network.SuggestRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(sessionManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _cartCount = MutableStateFlow(0)
    val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Book>>(emptyList())
    val searchResults: StateFlow<List<Book>> = _searchResults.asStateFlow()

    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())
    val allBooks: StateFlow<List<Book>> = _allBooks.asStateFlow()

    private val _suggestedBooks = MutableStateFlow<List<Book>>(emptyList())
    val suggestedBooks: StateFlow<List<Book>> = _suggestedBooks.asStateFlow()

    private val _sidebarBooks = MutableStateFlow<List<Book>>(emptyList())
    val sidebarBooks: StateFlow<List<Book>> = _sidebarBooks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _displayedCount = MutableStateFlow(12)
    val displayedCount: StateFlow<Int> = _displayedCount.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isChatBotVisible = MutableStateFlow(true)
    val isChatBotVisible: StateFlow<Boolean> = _isChatBotVisible.asStateFlow()

    init {
        loadHomeData()
        startSidebarRotation()
        refreshCounts()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getAllBooks()
                _allBooks.value = response.filter { it.trangThai != "DA_XOA" }.map { it.toBook() }

                val userId = sessionManager.getUserId()
                if (userId != null) {
                    val keywords = listOf("sách mới", "phổ biến")
                    val suggestions = apiService.getSuggestedBooks(SuggestRequest(userId, keywords))
                    var mappedSuggestions = suggestions.map { it.toBook() }

                    if (mappedSuggestions.size < 6) {
                        val fillers = _allBooks.value.take(6 - mappedSuggestions.size)
                        mappedSuggestions = mappedSuggestions + fillers
                    }
                    _suggestedBooks.value = mappedSuggestions
                } else {
                    _suggestedBooks.value = _allBooks.value.take(6)
                }

                updateSidebarBooks()
            } catch (e: Exception) {
                updateSidebarBooks()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateSidebarBooks() {
        if (_allBooks.value.isNotEmpty()) {
            _sidebarBooks.value = _allBooks.value.shuffled().take(4)
        }
    }

    private fun startSidebarRotation() {
        viewModelScope.launch {
            while (true) {
                delay(10000)
                updateSidebarBooks()
            }
        }
    }

    fun loadMoreBooks() {
        _displayedCount.value += 12
    }

    fun searchBooks(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.searchBooks(query)
                _searchResults.value = response.map { it.toBook() }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshCounts() {
        if (sessionManager.isLoggedIn()) {
            loadCartCount()
            loadNotificationCount()
        }
    }

    private fun loadCartCount() {
        val userId = sessionManager.getUserId() ?: return
        viewModelScope.launch {
            try {
                val response = apiService.getCart(userId)
                val count = response.data?.size ?: 0
                _cartCount.value = count
            } catch (_: Exception) { }
        }
    }

    private fun loadNotificationCount() {
        val userId = sessionManager.getUserId() ?: return
        viewModelScope.launch {
            try {
                val notifications = apiService.getNotifications(userId)
                val localRead = sessionManager.getReadNotifications()
                // Đồng bộ logic đếm chưa đọc với NotificationViewModel: 
                // Coi là đã đọc nếu Server bảo đã đọc HOẶC Local bảo đã đọc
                val unreadCount = notifications.count { item ->
                    !item.isRead && !localRead.contains(item.id.toString())
                }
                _unreadNotificationCount.value = unreadCount
            } catch (_: Exception) { }
        }
    }

    fun setCartCount(count: Int) {
        _cartCount.value = count
    }

    fun setUnreadNotificationCount(count: Int) {
        _unreadNotificationCount.value = count
    }

    fun setLoggedIn(value: Boolean) {
        _isLoggedIn.value = value
        if (value) {
            loadHomeData()
            refreshCounts()
        }
    }

    fun setChatBotVisibility(visible: Boolean) {
        _isChatBotVisible.value = visible
    }

    fun logout() {
        sessionManager.clearSession()
        _isLoggedIn.value = false
        _cartCount.value = 0
        _unreadNotificationCount.value = 0
        _suggestedBooks.value = _allBooks.value.take(6)
    }

    private fun BookResponse.toBook() = Book(
        id = maSach.toString(),
        title = tenSach,
        author = tenTacGia,
        publisher = nxb,
        year = nam,
        imageSrc = hinhAnh?.firstOrNull()?.trim() ?: "",
        available = trangThai == "CON_SAN",
        borrowCount = if (soLuongMuon >= 0) soLuongMuon else 0
    )
}