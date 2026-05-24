package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditCategoryChildViewModel(
    private val apiService: ApiService,
    private val childId: String
) : ViewModel() {

    private val _childName = MutableStateFlow("")
    val childName = _childName.asStateFlow()

    private val _parentName = MutableStateFlow("")
    val parentName = _parentName.asStateFlow()

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
                val child = apiService.getChildCategoryById(childId)
                _childName.value = child.name
                _parentName.value = child.parentName ?: "Không xác định"
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onChildNameChange(name: String) {
        _childName.value = name
    }

    fun updateChild() {
        if (_childName.value.isBlank()) {
            _message.value = "Tên danh mục không được để trống"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.updateChildCategory(childId, mapOf("name" to _childName.value))
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

    fun deleteChild() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.deleteChildCategory(childId)
                if (response.isSuccessful) {
                    _message.value = "Đã xóa danh mục con"
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
