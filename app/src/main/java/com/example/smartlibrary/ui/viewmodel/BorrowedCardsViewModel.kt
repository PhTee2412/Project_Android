package com.example.smartlibrary.ui.viewmodel

import android.util.Log
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

    private val _selectedTab = MutableStateFlow("Đã yêu cầu")
    val selectedTab = _selectedTab.asStateFlow()

    val filteredCards: StateFlow<List<BorrowCardResponse>> = combine(_borrowCards, _selectedTab) { cards, tab ->
        cards.filter { card ->
            val actualStatus = when (card.status) {
                null -> "Đã yêu cầu"               // nếu không có trạng thái, coi như mới yêu cầu
                "DA_YEU_CAU" -> "Đã yêu cầu"
                "DANG_MUON" -> "Đang mượn"
                "HET_HAN" -> "Hết hạn"
                else -> card.status
            }
            actualStatus == tab
        }
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

    fun loadBorrowCards() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            try {
                val response = apiService.getBorrowCardsByUser(userId)
                _borrowCards.value = response
            } catch (e: Exception) {
                _message.value = "Lỗi khi tải dữ liệu: ${e.localizedMessage}"
                Log.e("BorrowedCardsVM", "Lỗi khi tải phiếu mượn", e)
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
            _message.value = null
            try {
                val detail = apiService.getBorrowCardById(cardId.toString())
                _cardDetail.value = detail
            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                val errorBody = e.response()?.errorBody()?.string()
                when (code) {
                    401, 403 -> _message.value = "Bạn chưa đăng nhập hoặc phiên đã hết hạn. Vui lòng đăng nhập lại."
                    404 -> _message.value = "Không tìm thấy phiếu mượn (404)"
                    else -> _message.value = "Lỗi khi tải chi tiết (code=$code): ${errorBody ?: "Không có nội dung"}"
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

    fun clearBorrowCards() {
        _borrowCards.value = emptyList()
        _cardDetail.value = null
        _isDetailLoaded.value = false
    }
}