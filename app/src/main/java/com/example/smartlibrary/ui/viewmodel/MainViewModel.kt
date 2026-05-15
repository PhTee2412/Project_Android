package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.model.Book
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.network.SuggestRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val apiService: ApiService) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
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

    private val _isChatBotVisible = MutableStateFlow(true)
    val isChatBotVisible: StateFlow<Boolean> = _isChatBotVisible.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadHomeData()
        startSidebarRotation()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load all books first
                val response = apiService.getAllBooks()
                _allBooks.value = response.filter { it.trangThai != "DA_XOA" }.map { it.toBook() }
                println("Loaded ${response.size} books from API")

                // If no books loaded, add some mock data
                if (_allBooks.value.isEmpty()) {
                    _allBooks.value = getMockBooks()
                    println("Using mock data for allBooks")
                }

                // Giả lập lấy userId và keywords
                val userId = "user123" 
                val keywords = listOf("java", "kotlin")
                
                val suggestions = apiService.getSuggestedBooks(SuggestRequest(userId, keywords))
                println("Loaded ${suggestions.size} suggestions from API")
                var mappedSuggestions = suggestions.map { it.toBook() }
                
                if (mappedSuggestions.size < 6) {
                    val existingIds = mappedSuggestions.map { it.id }.toSet()
                    val fillers = _allBooks.value.filter { !existingIds.contains(it.id) }.take(6 - mappedSuggestions.size)
                    mappedSuggestions = mappedSuggestions + fillers
                }
                _suggestedBooks.value = mappedSuggestions
                
                updateSidebarBooks()
            } catch (e: Exception) {
                e.printStackTrace()
                println("API failed, using mock data")
                // If API fails, load mock data
                _allBooks.value = getMockBooks()
                _suggestedBooks.value = getMockBooks().take(6)
                updateSidebarBooks()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            loadBooksInternal()
            _isLoading.value = false
        }
    }

    private suspend fun loadBooksInternal() {
        try {
            val response = apiService.getAllBooks()
            _allBooks.value = response.filter { it.trangThai != "DA_XOA" }.map { it.toBook() }
        } catch (e: Exception) {
            // handle error
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

    fun setChatBotVisibility(visible: Boolean) {
        _isChatBotVisible.value = visible
    }

    fun showChatBot() {
        _isChatBotVisible.value = true
    }

    private fun BookResponse.toBook() = Book(
        id = maSach,
        title = tenSach,
        author = tenTacGia,
        publisher = nxb,
        year = nam,
        imageSrc = hinhAnh?.firstOrNull()?.trim() ?: "",
        available = (tongSoLuong - soLuongMuon - soLuongXoa) > 0,
        borrowCount = if (soLuongMuon >= 0) soLuongMuon else 0
    )

    fun toggleLogin() {
        _isLoggedIn.value = !_isLoggedIn.value
    }

    private fun getMockBooks(): List<Book> {
        // Return a list of mock books
        return listOf(
            Book("1", "Mock Book 1", "Author 1", "Publisher 1", 2021, "", true, 0),
            Book("2", "Mock Book 2", "Author 2", "Publisher 2", 2022, "", true, 0),
            Book("3", "Mock Book 3", "Author 3", "Publisher 3", 2023, "", true, 0),
            Book("4", "Mock Book 4", "Author 4", "Publisher 4", 2024, "", true, 0),
            Book("5", "Mock Book 5", "Author 5", "Publisher 5", 2025, "", true, 0),
            Book("6", "Mock Book 6", "Author 6", "Publisher 6", 2026, "", true, 0)
        )
    }
}
