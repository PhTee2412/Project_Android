package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BorrowCardResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun getFilteredCards(): List<BorrowCardResponse> {
        val filteredByTab = _allCards.value.filter { card ->
            val status = card.status?.uppercase() ?: ""
            when (_selectedTab.value) {
                "Đã yêu cầu" -> status == "ĐÃ YÊU CẦU" || status == "REQUESTED"
                "Đang mượn" -> status == "ĐANG MƯỢN" || status == "BORROWED"
                "Đã trả" -> status == "ĐÃ TRẢ" || status == "RETURNED"
                else -> false
            }
        }

        return if (_searchQuery.value.isBlank()) {
            filteredByTab
        } else {
            val query = _searchQuery.value.trim().lowercase()
            filteredByTab.filter {
                it.id.toString().contains(query) ||
                        it.userId.toString().contains(query)
            }
        }
    }

    fun getPaginatedCards(): List<BorrowCardResponse> {
        val filtered = getFilteredCards()
        val start = (_currentPage.value - 1) * itemsPerPage
        val end = minOf(start + itemsPerPage, filtered.size)
        return if (start < filtered.size) filtered.subList(start, end) else emptyList()
    }

    fun getTotalPages(): Int {
        val filteredSize = getFilteredCards().size
        return if (filteredSize == 0) 1 else kotlin.math.ceil(filteredSize.toDouble() / itemsPerPage).toInt()
    }

    fun markExpired() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val today = Date()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                
                // Lọc các phiếu quá hạn lấy sách
                val expiredList = getFilteredCards().filter { card ->
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
                val list = getFilteredCards()
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
