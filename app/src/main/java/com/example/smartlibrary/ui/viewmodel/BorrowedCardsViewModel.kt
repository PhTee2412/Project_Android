package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.SessionManager
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BorrowCardResponse
import com.example.smartlibrary.network.BorrowCardDetailResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BorrowedCardsViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _borrowCards = MutableStateFlow<List<BorrowCardResponse>>(emptyList())
    val borrowCards = _borrowCards.asStateFlow()

    private val _selectedTab = MutableStateFlow("DA_YEU_CAU") // Sử dụng mã code thay vì tiếng Việt
    val selectedTab = _selectedTab.asStateFlow()

    val filteredCards: StateFlow<List<BorrowCardResponse>> = combine(_borrowCards, _selectedTab) { cards, tab ->
        cards.filter { it.status == tab }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _cardDetail = MutableStateFlow<BorrowCardDetailResponse?>(null)
    val cardDetail = _cardDetail.asStateFlow()

    private val _isDetailLoaded = MutableStateFlow(false)
    val isDetailLoaded = _isDetailLoaded.asStateFlow()

    private val userId: String
        get() = sessionManager.getUserId() ?: ""

    init {
        loadBorrowCards()
    }

    fun loadBorrowCards() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getBorrowCardsByUser(userId)
                _borrowCards.value = response
            } catch (e: Exception) {
                _message.value = "Lỗi khi tải dữ liệu: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onTabSelected(tab: String) {
        _selectedTab.value = tab
    }

    fun loadCardDetail(cardId: Int) {
        viewModelScope.launch {
            _cardDetail.value = null
            _isDetailLoaded.value = false
            _isLoading.value = true
            try {
                val response = apiService.getBorrowCardById(cardId.toString())
                // Defensive: if server returned an empty object or unexpected data, surface helpful message
                if (response == null) {
                    _message.value = "Không tìm thấy chi tiết phiếu mượn (server trả về rỗng)"
                } else {
                    _cardDetail.value = response
                }
            } catch (e: Exception) {
                _message.value = "Lỗi khi tải chi tiết: ${e.localizedMessage}"
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
                val response = apiService.deleteBorrowCard(cardId.toString())
                if (response.isSuccessful) {
                    _message.value = "Xóa phiếu thành công"
                    _borrowCards.value = _borrowCards.value.filter { it.id != cardId }
                    onSuccess()
                } else {
                    _message.value = "Không thể xóa phiếu"
                }
            } catch (e: Exception) {
                _message.value = "Xóa phiếu thất bại: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}