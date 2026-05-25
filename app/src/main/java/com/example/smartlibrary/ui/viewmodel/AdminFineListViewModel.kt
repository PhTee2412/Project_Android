package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.FineResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FineListViewModel(private val apiService: ApiService) : ViewModel() {
    private val _allFines = MutableStateFlow<List<FineResponse>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _currentTab = MutableStateFlow("CHUA_THANH_TOAN")
    val currentTab = _currentTab.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage = _currentPage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    val itemsPerPage = 10

    // Dữ liệu đã lọc theo Tab và Search
    val filteredFines = combine(_allFines, _currentTab, _searchQuery) { fines, tab, query ->
        fines.filter { it.trangThai == tab }
            .filter { fine ->
                query.isEmpty() || 
                fine.id.toString().contains(query, ignoreCase = true) || 
                fine.userId?.toString()?.contains(query, ignoreCase = true) == true
            }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Dữ liệu sau khi phân trang
    val paginatedFines = combine(filteredFines, _currentPage) { filtered, page ->
        val fromIndex = (page - 1) * itemsPerPage
        if (fromIndex >= filtered.size) emptyList()
        else filtered.drop(fromIndex).take(itemsPerPage)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalPages = filteredFines.map { 
        val pages = Math.ceil(it.size.toDouble() / itemsPerPage).toInt()
        if (pages == 0) 1 else pages
    }.stateIn(viewModelScope, SharingStarted.Lazily, 1)

    init {
        loadFines()
    }

    fun loadFines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allFines.value = apiService.getAllFines()
            } catch (e: Exception) {
                _message.value = "Lỗi tải danh sách: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onTabChange(tab: String) {
        _currentTab.value = tab
        _currentPage.value = 1
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _currentPage.value = 1
    }

    fun onPageChange(page: Int) {
        _currentPage.value = page
    }

    fun clearMessage() { _message.value = null }
}
