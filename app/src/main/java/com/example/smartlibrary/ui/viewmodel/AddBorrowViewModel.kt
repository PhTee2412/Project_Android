package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.network.CreateBorrowCardRequest
import com.example.smartlibrary.network.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddBorrowViewModel(private val apiService: ApiService) : ViewModel() {

    private val _userIdInput = MutableStateFlow("")
    val userIdInput = _userIdInput.asStateFlow()

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser = _selectedUser.asStateFlow()

    private val _bookList = MutableStateFlow<List<BookResponse>>(emptyList())
    val bookList = _bookList.asStateFlow()

    private val _selectedBook = MutableStateFlow<BookResponse?>(null)
    val selectedBook = _selectedBook.asStateFlow()

    private val _borrowList = MutableStateFlow<List<BookResponse>>(emptyList())
    val borrowList = _borrowList.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _onSuccess = MutableStateFlow(false)
    val onSuccess = _onSuccess.asStateFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getAllBooks()
                _bookList.value = response
            } catch (e: Exception) {
                _message.value = "Lỗi tải danh sách sách"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onUserIdInputChange(value: String) {
        _userIdInput.value = value
    }

    fun findUser() {
        if (_userIdInput.value.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = apiService.getUserProfile(_userIdInput.value.trim())
                _selectedUser.value = user
                _message.value = "Đã chọn người dùng: ${user.username}"
            } catch (e: Exception) {
                _selectedUser.value = null
                _message.value = "Không tìm thấy người dùng"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onBookSelected(book: BookResponse?) {
        _selectedBook.value = book
    }

    fun addBook() {
        val book = _selectedBook.value ?: return
        if (_borrowList.value.any { it.maSach == book.maSach }) {
            _message.value = "Sách này đã có trong danh sách"
            return
        }
        _borrowList.value = _borrowList.value + book
        _selectedBook.value = null
        _message.value = "Đã thêm sách vào danh sách"
    }

    fun removeBook(book: BookResponse) {
        _borrowList.value = _borrowList.value.filter { it.maSach != book.maSach }
    }

    fun submit() {
        val user = _selectedUser.value
        if (user == null) {
            _message.value = "Vui lòng chọn người dùng"
            return
        }
        if (_borrowList.value.isEmpty()) {
            _message.value = "Vui lòng thêm sách mượn"
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val now = sdf.format(Date())
                
                // Deadline 7 days
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 7)
                val dueDate = sdf.format(calendar.time)

                val request = CreateBorrowCardRequest(
                    userId = user.id,
                    bookIds = _borrowList.value.map { it.maSach },
                    borrowDate = now,
                    dueDate = dueDate
                )
                
                val response = apiService.createBorrowCard(request)
                if (response.isSuccessful) {
                    _message.value = "Tạo phiếu mượn thành công"
                    _onSuccess.value = true
                } else {
                    _message.value = "Lỗi khi tạo phiếu mượn"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
