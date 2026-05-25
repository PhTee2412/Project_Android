package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddFineViewModel(private val apiService: ApiService) : ViewModel() {
    private val _userText = MutableStateFlow("")
    val userText = _userText.asStateFlow()

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser = _selectedUser.asStateFlow()

    private val _money = MutableStateFlow("")
    val money = _money.asStateFlow()

    private val _reason = MutableStateFlow("") // "Trả sách trễ hạn", "Làm mất sách", "Khác"
    val reason = _reason.asStateFlow()

    private val _borrowList = MutableStateFlow<List<BorrowCardResponse>>(emptyList())
    val borrowList = _borrowList.asStateFlow()

    private val _selectedBorrow = MutableStateFlow<BorrowCardResponse?>(null)
    val selectedBorrow = _selectedBorrow.asStateFlow()

    private val _bookText = MutableStateFlow("")
    val bookText = _bookText.asStateFlow()

    private val _selectedBook = MutableStateFlow<BookResponse?>(null)
    val selectedBook = _selectedBook.asStateFlow()

    private val _otherContent = MutableStateFlow("")
    val otherContent = _otherContent.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    fun onUserTextChange(text: String) { _userText.value = text }
    fun onMoneyChange(text: String) { _money.value = text }
    fun onReasonChange(text: String) {
        _reason.value = text
        _selectedBorrow.value = null
        _selectedBook.value = null
        _otherContent.value = ""
    }
    fun onBorrowSelected(borrow: BorrowCardResponse) { _selectedBorrow.value = borrow }
    fun onBookTextChange(text: String) { _bookText.value = text }
    fun onOtherContentChange(text: String) { _otherContent.value = text }

    fun findUser() {
        if (_userText.value.isBlank()) return
        viewModelScope.launch {
            try {
                // Giả sử có API tìm user theo ID hoặc lấy tất cả rồi lọc
                // Theo code Next.js: lọc từ userList
                val users = apiService.getAllUsers()
                val user = users.find { it.id.toString() == _userText.value.trim() }
                if (user != null) {
                    _selectedUser.value = user
                    loadBorrowCards(user.id.toString())
                } else {
                    _message.value = "Không tìm thấy người dùng với ID này"
                    _selectedUser.value = null
                }
            } catch (e: Exception) {
                _message.value = "Lỗi tìm người dùng: ${e.localizedMessage}"
            }
        }
    }

    private fun loadBorrowCards(userId: String) {
        viewModelScope.launch {
            try {
                _borrowList.value = apiService.getBorrowCardsByUser(userId)
            } catch (e: Exception) {
                _borrowList.value = emptyList()
            }
        }
    }

    fun findBook() {
        if (_bookText.value.isBlank()) return
        viewModelScope.launch {
            try {
                // API find book by child ID (barcode/id)
                val book = apiService.getChildBookById(_bookText.value.trim())
                _selectedBook.value = book
            } catch (e: Exception) {
                _message.value = "Không tìm thấy sách với ID này"
                _selectedBook.value = null
            }
        }
    }

    fun submit() {
        val user = _selectedUser.value ?: return run { _message.value = "Vui lòng chọn Người dùng" }
        val amount = _money.value.toDoubleOrNull() ?: return run { _message.value = "Vui lòng nhập số tiền hợp lệ" }
        if (_reason.value.isBlank()) return run { _message.value = "Vui lòng chọn lý do phạt" }

        var cardIdValue: Any? = null
        when (_reason.value) {
            "Trả sách trễ hạn" -> {
                cardIdValue = _selectedBorrow.value?.id ?: return run { _message.value = "Vui lòng chọn Phiếu mượn" }
            }
            "Làm mất sách" -> {
                cardIdValue = _selectedBook.value?.maSach ?: return run { _message.value = "Vui lòng nhập và kiểm tra ID sách" }
            }
            "Khác" -> {
                if (_otherContent.value.isBlank()) return run { _message.value = "Vui lòng nhập nội dung phạt" }
                cardIdValue = _otherContent.value
            }
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val payload = AddFineRequest(
                    userId = user.id.toInt(),
                    soTien = amount,
                    noiDung = _reason.value,
                    cardId = cardIdValue
                )
                val response = apiService.addFine(payload)
                if (response.isSuccessful) {
                    _isSuccess.value = true
                    _message.value = "Thêm phiếu phạt thành công!"
                } else {
                    _message.value = "Lỗi khi tạo phiếu phạt"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.localizedMessage}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun clearMessage() { _message.value = null }
}
