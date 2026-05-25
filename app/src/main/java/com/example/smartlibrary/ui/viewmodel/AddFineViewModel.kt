package com.example.smartlibrary.ui.viewmodel

import android.util.Log
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

    private val _reason = MutableStateFlow("")
    val reason = _reason.asStateFlow()

    private val _borrowList = MutableStateFlow<List<BorrowCardResponse>>(emptyList())
    val borrowList = _borrowList.asStateFlow()

    private val _selectedBorrow = MutableStateFlow<BorrowCardResponse?>(null)
    val selectedBorrow = _selectedBorrow.asStateFlow()

    private val _bookText = MutableStateFlow("")
    val bookText = _bookText.asStateFlow()

    private val _bookFound = MutableStateFlow(false) // Thay vì BookResponse
    val bookFound = _bookFound.asStateFlow()

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
        _bookFound.value = false
        _otherContent.value = ""
    }
    fun onBorrowSelected(borrow: BorrowCardResponse) { _selectedBorrow.value = borrow }
    fun onBookTextChange(text: String) {
        _bookText.value = text
        _bookFound.value = false // reset mỗi khi thay đổi input
    }
    fun onOtherContentChange(text: String) { _otherContent.value = text }

    fun findUser() {
        if (_userText.value.isBlank()) return
        viewModelScope.launch {
            try {
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
        val id = _bookText.value.trim()
        if (id.isBlank()) {
            _message.value = "Vui lòng nhập ID sách"
            return
        }
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                // Gọi API kiểm tra sự tồn tại (không cần parse dữ liệu)
                val book = apiService.getChildBookById(id)
                // Nếu không có exception, coi như tìm thấy
                _bookFound.value = true
                _message.value = "Đã tìm thấy sách (ID: $id)"
            } catch (e: Exception) {
                Log.e("AddFine", "Lỗi kiểm tra sách", e)
                _bookFound.value = false
                _message.value = "Không tìm thấy sách với ID này"
            } finally {
                _isSubmitting.value = false
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
                if (_bookText.value.isBlank() || !_bookFound.value)
                    return run { _message.value = "Vui lòng nhập và kiểm tra ID sách" }
                cardIdValue = _bookText.value // Lưu ID sách con dạng String
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
                    val errorBody = response.errorBody()?.string() ?: "Không rõ lỗi"
                    Log.e("AddFine", "Submit thất bại: $errorBody")
                    _message.value = "Lỗi khi tạo phiếu phạt ($errorBody)"
                }
            } catch (e: Exception) {
                Log.e("AddFine", "Submit lỗi", e)
                _message.value = "Lỗi: ${e.localizedMessage}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun clearMessage() { _message.value = null }
}