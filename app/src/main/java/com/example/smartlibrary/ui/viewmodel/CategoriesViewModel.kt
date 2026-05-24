package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.data.model.Book
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.network.CategoryChildResponse
import com.example.smartlibrary.network.CategoryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(private val apiService: ApiService) : ViewModel() {

    // Danh mục cha từ API
    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories.asStateFlow()

    // Danh mục con được tạo động khi chọn danh mục cha
    private val _categoryChildren = MutableStateFlow<List<CategoryChildResponse>>(emptyList())
    val categoryChildren: StateFlow<List<CategoryChildResponse>> = _categoryChildren.asStateFlow()

    // Sách đã lọc và map sang Book
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    // Trạng thái lọc
    private val _selectedParentId = MutableStateFlow<String?>(null)
    val selectedParentId: StateFlow<String?> = _selectedParentId.asStateFlow()

    private val _selectedChildId = MutableStateFlow<String?>(null)
    val selectedChildId: StateFlow<String?> = _selectedChildId.asStateFlow()

    private val _activeFilter = MutableStateFlow("ALL")
    val activeFilter: StateFlow<String> = _activeFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Toàn bộ sách (đã bỏ sách bị xóa)
    private var allBookResponses: List<BookResponse> = emptyList()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // 1. Tải danh mục cha
                val categoriesResult = apiService.getCategories()
                _categories.value = categoriesResult

                // 2. Tải toàn bộ sách
                val booksResponse = apiService.getAllBooks()
                allBookResponses = booksResponse.filter { it.trangThai != "DA_XOA" }

                // 3. Mặc định hiển thị tất cả
                applyFilters()
            } catch (e: Exception) {
                _error.value = "Không thể tải dữ liệu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectParentCategory(parentId: String?) {
        _selectedParentId.value = parentId
        _selectedChildId.value = null

        if (parentId != null) {
            val parentName = _categories.value.find { it.id == parentId }?.name ?: ""
            // Tạo danh sách con từ sách
            val childrenMap = mutableMapOf<String, String>() // childId -> childName
            for (book in allBookResponses) {
                if (book.categoryParentName == parentName && book.categoryChildId != null) {
                    val childId = book.categoryChildId!!
                    val childName = book.categoryChildName ?: "Unknown"
                    childrenMap[childId] = childName
                }
            }
            _categoryChildren.value = childrenMap.map { (id, name) ->
                CategoryChildResponse(
                    id = id,
                    name = name,
                    parentId = parentId,
                    parentName = parentName
                )
            }.sortedBy { it.name } // sắp xếp theo tên
        } else {
            _categoryChildren.value = emptyList()
        }
        applyFilters()
    }

    fun selectChildCategory(childId: String?) {
        // Chọn / bỏ chọn danh mục con
        _selectedChildId.value = if (_selectedChildId.value == childId) null else childId
        applyFilters()
    }

    fun setFilter(filter: String) {
        _activeFilter.value = filter
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = allBookResponses

        // Lọc theo danh mục con (ưu tiên)
        val childId = _selectedChildId.value
        if (childId != null) {
            filtered = filtered.filter { it.categoryChildId == childId }
        } else {
            // Lọc theo danh mục cha
            val parentId = _selectedParentId.value
            if (parentId != null) {
                val parentName = _categories.value.find { it.id == parentId }?.name ?: ""
                filtered = filtered.filter { it.categoryParentName == parentName }
            }
        }

        // Áp dụng bộ lọc sắp xếp (Mới nhất, Mượn nhiều)
        filtered = when (_activeFilter.value) {
            "NEWEST" -> filtered.sortedByDescending { it.nam ?: 0 }
            "MOST_BORROWED" -> filtered.sortedByDescending { it.soLuongMuon }
            else -> filtered
        }

        _books.value = filtered.map { it.toBook() }
    }

    private fun BookResponse.toBook() = Book(
        id = maSach.toString(),
        title = tenSach,
        author = tenTacGia,
        publisher = nxb,
        year = nam,
        imageSrc = hinhAnh?.firstOrNull()?.trim() ?: "",
        // ĐỒNG BỘ TUYỆT ĐỐI: Tin tưởng hoàn toàn vào trangThai từ Backend (như MainViewModel)
        available = trangThai == "CON_SAN",
        borrowCount = if (soLuongMuon >= 0) soLuongMuon else 0
    )
}
