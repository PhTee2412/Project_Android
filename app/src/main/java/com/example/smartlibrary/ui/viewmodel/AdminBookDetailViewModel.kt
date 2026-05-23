package com.example.smartlibrary.ui.viewmodel

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.network.ChildBookResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminBookDetailViewModel(
    private val apiService: ApiService,
    private val bookId: Long
) : ViewModel() {

    private val _book = MutableStateFlow<BookResponse?>(null)
    val book: StateFlow<BookResponse?> = _book.asStateFlow()

    private val _childBooks = MutableStateFlow<List<ChildBookResponse>>(emptyList())
    val childBooks: StateFlow<List<ChildBookResponse>> = _childBooks.asStateFlow()

    private val _filteredChildBooks = MutableStateFlow<List<ChildBookResponse>>(emptyList())
    val filteredChildBooks: StateFlow<List<ChildBookResponse>> = _filteredChildBooks.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _actionLoading = MutableStateFlow(false)
    val actionLoading: StateFlow<Boolean> = _actionLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _deleteTarget = MutableStateFlow<ChildBookResponse?>(null)
    val deleteTarget: StateFlow<ChildBookResponse?> = _deleteTarget.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load book detail
                val bookResponse = apiService.getBookById(bookId.toString())
                _book.value = bookResponse

                // Load child books
                val children = apiService.getChildBooks(bookId)
                _childBooks.value = children
                _filteredChildBooks.value = children
                searchChildBooks() // Re-apply filter if needed
            } catch (e: Exception) {
                _message.value = "Lỗi khi tải dữ liệu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchChildBooks()
    }

    fun searchChildBooks() {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) {
            _filteredChildBooks.value = _childBooks.value
        } else {
            _filteredChildBooks.value = _childBooks.value.filter {
                it.id.contains(query, ignoreCase = true) ||
                        (it.barcode?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    fun addChildBook() {
        viewModelScope.launch {
            _actionLoading.value = true
            try {
                val response = apiService.addChildBook(bookId)
                if (response.isSuccessful) {
                    _message.value = "Đã tạo sách con mới"
                    loadData()
                } else {
                    _message.value = "Không thể thêm sách con"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    fun requestDelete(child: ChildBookResponse) {
        _deleteTarget.value = child
    }

    fun cancelDelete() {
        _deleteTarget.value = null
    }

    fun confirmDelete() {
        val target = _deleteTarget.value ?: return
        val targetId = target.id
        _deleteTarget.value = null
        
        viewModelScope.launch {
            _actionLoading.value = true
            try {
                val response = apiService.deleteChildBook(targetId)
                if (response.isSuccessful) {
                    _message.value = "Xóa sách con thành công"
                    
                    // Cập nhật trạng thái cục bộ thay vì xóa khỏi list
                    val updatedList = _childBooks.value.map { 
                        if (it.id == targetId) it.copy(status = "DA_XOA") else it 
                    }
                    _childBooks.value = updatedList
                    searchChildBooks()
                    
                    // Cập nhật lại thông tin sách chính để refresh số lượng thống kê
                    try {
                        val bookResponse = apiService.getBookById(bookId.toString())
                        _book.value = bookResponse
                    } catch (e: Exception) {}
                } else {
                    _message.value = "Xóa thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    fun downloadBarcode(context: Context, barcode: String) {
        try {
            val url = "https://barcodeapi.org/api/128/$barcode"
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Barcode-$barcode")
                .setDescription("Đang tải xuống barcode...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "barcode-$barcode.png")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            _message.value = "Đang bắt đầu tải xuống barcode..."
        } catch (e: Exception) {
            _message.value = "Không thể tải xuống: ${e.message}"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
