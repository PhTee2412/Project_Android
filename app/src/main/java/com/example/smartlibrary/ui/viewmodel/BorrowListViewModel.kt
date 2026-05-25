package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BorrowCardResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BorrowListViewModel(private val apiService: ApiService) : ViewModel() {

    private val _allCards = MutableStateFlow<List<BorrowCardResponse>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow("Đã yêu cầu")
    val selectedTab = _selectedTab.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage = _currentPage.asStateFlow()

    private val itemsPerPage = 10

    // Lọc dữ liệu phản ứng (tương tự useMemo trong Next.js)
    val filteredCards = combine(_allCards, _selectedTab, _searchQuery) { cards, tab, query ->
        val filteredByTab = cards.filter { card ->
            val status = card.status ?: ""
            when (tab) {
                "Đã yêu cầu" -> status.equals("Đã yêu cầu", ignoreCase = true) || status.equals("REQUESTED", ignoreCase = true)
                "Đang mượn" -> status.equals("Đang mượn", ignoreCase = true) || status.equals("BORROWED", ignoreCase = true) || status.equals("DANG_MUON", ignoreCase = true)
                "Đã trả" -> status.equals("Đã trả", ignoreCase = true) || status.equals("RETURNED", ignoreCase = true) || status.equals("DA_TRA", ignoreCase = true)
                else -> false
            }
        }
        
        if (query.isBlank()) {
            filteredByTab
        } else {
            val lowerQuery = query.trim().lowercase()
            filteredByTab.filter {
                it.id.toString().contains(lowerQuery) ||
                it.userId.toString().contains(lowerQuery)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dữ liệu đã phân trang
    val paginatedCards = combine(filteredCards, _currentPage) { filtered, page ->
        val start = (page - 1) * itemsPerPage
        val end = minOf(start + itemsPerPage, filtered.size)
        if (start < filtered.size) filtered.subList(start, end) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tổng số trang
    val totalPages = filteredCards.map { 
        val pages = if (it.isEmpty()) 1 else kotlin.math.ceil(it.size.toDouble() / itemsPerPage).toInt()
        pages
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    init {
        fetchBorrowCards()
    }

    fun fetchBorrowCards() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getAllBorrowCards()
                _allCards.value = response
            } catch (e: Exception) {
                _message.value = "Lỗi khi tải dữ liệu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _currentPage.value = 1
    }

    fun onTabSelected(tab: String) {
        _selectedTab.value = tab
        _currentPage.value = 1
        _searchQuery.value = ""
    }

    fun onPageChange(page: Int) {
        _currentPage.value = page
    }

    fun markExpired() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val today = Date()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val expiredList = filteredCards.value.filter { card ->
                    val getBookDateStr = card.getBookDate
                    if (getBookDateStr != null) {
                        try {
                            val getBookDate = sdf.parse(getBookDateStr.substringBefore("T"))
                            getBookDate != null && getBookDate.before(today)
                        } catch (e: Exception) {
                            false
                        }
                    } else false
                }
                if (expiredList.isEmpty()) {
                    _message.value = "Không có phiếu nào hết hạn"
                } else {
                    expiredList.forEach { card ->
                        apiService.markExpired(card.id.toString())
                    }
                    _message.value = "Đã xét phiếu hết hạn thành công"
                    fetchBorrowCards()
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun askToReturn() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = filteredCards.value
                if (list.isEmpty()) {
                    _message.value = "Không có phiếu nào đang mượn"
                } else {
                    apiService.askToReturn(list)
                    _message.value = "Đã gửi mail hối trả sách thành công"
                    fetchBorrowCards()
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
