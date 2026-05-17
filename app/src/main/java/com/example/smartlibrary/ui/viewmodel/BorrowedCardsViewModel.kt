package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BorrowCardResponse
import com.example.smartlibrary.network.BorrowedBookBrief
import com.example.smartlibrary.network.BorrowCardDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BorrowedCardsViewModel(private val apiService: ApiService) : ViewModel() {

    private val _borrowCards = MutableStateFlow<List<BorrowCardResponse>>(emptyList())
    val borrowCards = _borrowCards.asStateFlow()

    private val _selectedTab = MutableStateFlow("Đã yêu cầu")
    val selectedTab = _selectedTab.asStateFlow()

    private val _filteredCards = MutableStateFlow<List<BorrowCardResponse>>(emptyList())
    val filteredCards = _filteredCards.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _cardDetail = MutableStateFlow<BorrowCardDetailResponse?>(null)
    val cardDetail = _cardDetail.asStateFlow()

    // Thêm state để kiểm tra xem đã từng load detail chưa, tránh hiện "Không tìm thấy" lúc mới vào
    private val _isDetailLoaded = MutableStateFlow(false)
    val isDetailLoaded = _isDetailLoaded.asStateFlow()

    init {
        loadBorrowCards()
    }

    fun loadBorrowCards() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getBorrowCardsByUser("2")
                if (response.isEmpty()) {
                    _borrowCards.value = getMockData()
                } else {
                    _borrowCards.value = response
                }
                filterCards(_selectedTab.value)
            } catch (e: Exception) {
                _message.value = "Lỗi khi tải dữ liệu: ${e.message}"
                _borrowCards.value = getMockData()
                filterCards(_selectedTab.value)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onTabSelected(tab: String) {
        _selectedTab.value = tab
        filterCards(tab)
    }

    private fun filterCards(tab: String) {
        _filteredCards.value = _borrowCards.value.filter { it.status == tab }
    }

    fun loadCardDetail(cardId: Int) {
        viewModelScope.launch {
            _cardDetail.value = null
            _isDetailLoaded.value = false
            _isLoading.value = true
            try {
                val response = apiService.getBorrowCardById(cardId.toString())
                _cardDetail.value = response
            } catch (e: Exception) {
                _message.value = "Lỗi khi tải chi tiết: ${e.message}"
                val mockCard = _borrowCards.value.find { it.id == cardId }
                _cardDetail.value = mockCard?.let {
                    BorrowCardDetailResponse(
                        id = it.id,
                        userId = it.userId,
                        userName = "Người dùng Mock",
                        borrowDate = it.borrowDate,
                        dueDate = it.dueDate,
                        getBookDate = it.getBookDate,
                        status = it.status,
                        totalBooks = it.bookIds?.size ?: 0,
                        bookIds = it.bookIds
                    )
                }
            } finally {
                _isLoading.value = false
                _isDetailLoaded.value = true
            }
        }
    }

    fun deleteCard(cardId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                apiService.deleteBorrowCard(cardId.toString())
                _message.value = "Xóa phiếu thành công"
                loadBorrowCards()
                onSuccess()
            } catch (e: Exception) {
                _message.value = "Xóa phiếu thất bại: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun getMockData(): List<BorrowCardResponse> {
        val books = listOf(
            BorrowedBookBrief(
                bookId = 1,
                name = "Java Nâng Cao 1",
                author = "Đoàn Văn Ban",
                image = "https://example.com/java.jpg",
                category = "Sách Lập Trình",
                publisher = "NXB Trẻ",
                borrowCount = 5
            ),
            BorrowedBookBrief(
                bookId = 2,
                name = "Kotlin for Android",
                author = "JetBrains",
                image = "https://example.com/kotlin.jpg",
                category = "Công nghệ",
                publisher = "NXB Giáo dục",
                borrowCount = 10
            )
        )
        return listOf(
            BorrowCardResponse(14, 2, "2026-05-12", null, "2026-05-15", "Đã yêu cầu", 0, books),
            BorrowCardResponse(15, 2, "2024-05-10", "2024-05-24", null, "Đang mượn", 0, books),
            BorrowCardResponse(16, 2, "2024-04-01", "2024-04-15", null, "Hết hạn", 2, books)
        )
    }
}
