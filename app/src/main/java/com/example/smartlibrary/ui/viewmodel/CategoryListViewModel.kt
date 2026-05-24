package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.CategoryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryListViewModel(private val apiService: ApiService) : ViewModel() {

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getCategories()
                _categories.value = response
            } catch (e: Exception) {
                _message.value = "Lỗi tải danh mục: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteChild(childId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteChildCategory(childId)
                if (response.isSuccessful) {
                    _message.value = "Xóa danh mục con thành công"
                    loadCategories()
                } else {
                    _message.value = "Không thể xóa: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun deleteParent(parentId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteCategory(parentId)
                if (response.isSuccessful) {
                    _message.value = "Xóa danh mục cha thành công"
                    loadCategories()
                } else {
                    _message.value = "Không thể xóa: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
