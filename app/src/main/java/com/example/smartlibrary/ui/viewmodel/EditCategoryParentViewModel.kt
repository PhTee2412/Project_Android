package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.CategoryChildResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditCategoryParentViewModel(
    private val apiService: ApiService,
    private val parentId: String
) : ViewModel() {

    private val _parentName = MutableStateFlow("")
    val parentName = _parentName.asStateFlow()

    private val _children = MutableStateFlow<List<CategoryChildResponse>>(emptyList())
    val children = _children.asStateFlow()

    private val _newChildName = MutableStateFlow("")
    val newChildName = _newChildName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted = _isDeleted.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val category = apiService.getCategoryById(parentId)
                _parentName.value = category.name
                _children.value = category.children ?: emptyList()
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onParentNameChange(name: String) {
        _parentName.value = name
    }

    fun onNewChildNameChange(name: String) {
        _newChildName.value = name
    }

    fun updateParent() {
        if (_parentName.value.isBlank()) {
            _message.value = "Tên danh mục không được để trống"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.updateCategory(parentId, mapOf("name" to _parentName.value))
                if (response.isSuccessful) {
                    _message.value = "Cập nhật thành công"
                } else {
                    _message.value = "Lỗi: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addChild() {
        if (_newChildName.value.isBlank()) return
        viewModelScope.launch {
            try {
                val response = apiService.addChildCategory(parentId, mapOf("name" to _newChildName.value))
                if (response.isSuccessful) {
                    _newChildName.value = ""
                    loadData() // Refresh list
                } else {
                    _message.value = "Lỗi thêm: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun deleteChild(childId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteChildCategory(childId)
                if (response.isSuccessful) {
                    loadData() // Refresh list
                } else {
                    _message.value = "Lỗi xóa: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun deleteParent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.deleteCategory(parentId)
                if (response.isSuccessful) {
                    _message.value = "Đã xóa danh mục"
                    _isDeleted.value = true
                } else {
                    _message.value = "Lỗi xóa: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
