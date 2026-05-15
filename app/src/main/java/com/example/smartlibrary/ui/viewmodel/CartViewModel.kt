package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.network.BorrowRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(
    private val apiService: ApiService,
    private val onCartCountChanged: (Int) -> Unit
) : ViewModel() {

    private val _cartBooks = MutableStateFlow<List<BookResponse>>(emptyList())
    val cartBooks: StateFlow<List<BookResponse>> = _cartBooks.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Tạm thời dùng userId cứng, sau này thay bằng userId thực từ đăng nhập
    private val userId = "user123"

    init {
        loadCart()
    }

    private val mockBooks = listOf(
        BookResponse(
            maSach = "101",
            tenSach = "Dế Mèn Phiêu Lưu Ký",
            tenTacGia = "Tô Hoài",
            nxb = "NXB Kim Đồng",
            nam = 2020,
            hinhAnh = listOf("https://salt.tikicdn.com/cache/w1200/ts/product/45/3d/e4/da101340156942095f9e31d334e1e48f.jpg"),
            tongSoLuong = 10,
            moTa = "Một trong những tác phẩm kinh điển của văn học thiếu nhi Việt Nam."
        ),
        BookResponse(
            maSach = "102",
            tenSach = "Đắc Nhân Tâm",
            tenTacGia = "Dale Carnegie",
            nxb = "NXB Tổng hợp TP.HCM",
            nam = 2021,
            hinhAnh = listOf("https://salt.tikicdn.com/cache/w1200/ts/product/f4/04/ed/f9e01306b3a092d6e06f971206d4e253.jpg"),
            tongSoLuong = 5,
            moTa = "Cuốn sách về nghệ thuật giao tiếp và thu phục lòng người."
        ),
        BookResponse(
            maSach = "103",
            tenSach = "Sherlock Holmes Toàn Tập",
            tenTacGia = "Arthur Conan Doyle",
            nxb = "NXB Văn Học",
            nam = 2019,
            hinhAnh = listOf("https://salt.tikicdn.com/cache/w1200/ts/product/01/a3/9b/7f9435b69996d914d3f7895f87b3223f.jpg"),
            tongSoLuong = 3,
            moTa = "Hành trình phá án của vị thám tử tài ba nhất mọi thời đại."
        )
    )

    fun loadCart() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getCart(userId)
                val remoteBooks = response.data ?: emptyList()
                
                // Kết hợp dữ liệu từ API và Mock data để test UI
                val allBooks = (mockBooks + remoteBooks).distinctBy { it.maSach }
                _cartBooks.value = allBooks
                
                // Cập nhật số lượng giỏ hàng cho MainViewModel
                onCartCountChanged(allBooks.size)
                
                // Bỏ chọn các sách không còn trong giỏ
                val validIds = allBooks.map { it.maSach }.toSet()
                _selectedIds.value = _selectedIds.value.intersect(validIds)
            } catch (e: Exception) {
                // Nếu lỗi API, vẫn hiển thị Mock data để xem UI
                _cartBooks.value = mockBooks
                onCartCountChanged(mockBooks.size)
                _message.value = "Không thể tải giỏ hàng từ server, đang hiển thị dữ liệu mẫu."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSelection(bookId: String) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply {
            if (contains(bookId)) remove(bookId) else add(bookId)
        }
    }

    fun selectAll() {
        _selectedIds.value = _cartBooks.value.map { it.maSach }.toSet()
    }

    fun deselectAll() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        val idsToDelete = _selectedIds.value.toList()
        if (idsToDelete.isEmpty()) return

        // Nếu là mock data (ID 101, 102, 103), ta xóa local luôn để thấy kết quả
        val mockIds = listOf("101", "102", "103")
        val currentList = _cartBooks.value.toMutableList()
        val mockIdsToDelete = idsToDelete.filter { it in mockIds }
        
        if (mockIdsToDelete.isNotEmpty()) {
            currentList.removeAll { it.maSach in mockIdsToDelete }
            _cartBooks.value = currentList
            _selectedIds.value = _selectedIds.value - mockIdsToDelete.toSet()
            onCartCountChanged(currentList.size)
        }

        val remoteIdsToDelete = idsToDelete.filter { it !in mockIds }
        if (remoteIdsToDelete.isEmpty()) {
            if (mockIdsToDelete.isNotEmpty()) _message.value = "Đã xóa các mục mẫu."
            return
        }

        viewModelScope.launch {
            try {
                val response = apiService.removeBooksFromCart(userId, remoteIdsToDelete)
                if (response.isSuccessful) {
                    _message.value = "Đã xóa ${remoteIdsToDelete.size} sách khỏi giỏ."
                    loadCart()
                } else {
                    _message.value = "Không thể xóa sách khỏi giỏ."
                }
            } catch (e: Exception) {
                _message.value = "Lỗi khi xóa sách: ${e.message}"
            }
        }
    }

    fun borrowSelected() {
        val idsToBorrow = _selectedIds.value.toList()
        if (idsToBorrow.isEmpty()) return

        viewModelScope.launch {
            try {
                // Chuyển bookIds thành List<Int> theo yêu cầu của BorrowRequest
                val intIds = idsToBorrow.mapNotNull { it.toIntOrNull() }
                if (intIds.size != idsToBorrow.size) {
                    _message.value = "ID sách không hợp lệ (phải là số)."
                    return@launch
                }
                
                val response = apiService.borrowBook(
                    BorrowRequest(userId = userId, bookIds = intIds)
                )
                
                if (response.isSuccessful) {
                    // Xóa sách đã mượn khỏi giỏ
                    apiService.removeBooksFromCart(userId, idsToBorrow)
                    _message.value = "Tạo phiếu mượn thành công! Vui lòng chờ duyệt."
                    // Cập nhật lại danh sách giỏ hàng và số lượng
                    loadCart()
                } else {
                    // Đối với Mock data, giả lập thành công nếu API thật lỗi (để test UI)
                    if (idsToBorrow.all { it in listOf("101", "102", "103") }) {
                        _message.value = "[MOCK] Tạo phiếu mượn thành công cho dữ liệu mẫu!"
                        val currentList = _cartBooks.value.filterNot { it.maSach in idsToBorrow }
                        _cartBooks.value = currentList
                        _selectedIds.value = emptySet()
                        onCartCountChanged(currentList.size)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        _message.value = "Không thể tạo phiếu mượn: ${errorBody ?: "Lỗi không xác định"}"
                    }
                }
            } catch (e: Exception) {
                // Mock behavior on failure
                if (idsToBorrow.all { it in listOf("101", "102", "103") }) {
                    _message.value = "[MOCK] Tạo phiếu mượn thành công cho dữ liệu mẫu!"
                    val currentList = _cartBooks.value.filterNot { it.maSach in idsToBorrow }
                    _cartBooks.value = currentList
                    _selectedIds.value = emptySet()
                    onCartCountChanged(currentList.size)
                } else {
                    _message.value = "Lỗi khi mượn sách: ${e.message}"
                }
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
