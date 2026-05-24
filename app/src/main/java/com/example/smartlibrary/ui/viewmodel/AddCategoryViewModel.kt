package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.AddCategoryRequest
import com.example.smartlibrary.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddCategoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _parentName = MutableStateFlow("")
    val parentName = _parentName.asStateFlow()

    private val _childNames = MutableStateFlow(listOf(""))
    val childNames = _childNames.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    fun onParentNameChange(name: String) {
        _parentName.value = name
    }

    fun onChildNameChange(index: Int, name: String) {
        val currentList = _childNames.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = name
            _childNames.value = currentList
        }
    }

    fun addChild() {
        _childNames.value = _childNames.value + ""
    }

    fun removeChild(index: Int) {
        val currentList = _childNames.value.toMutableList()
        if (currentList.size > 1 && index in currentList.indices) {
            currentList.removeAt(index)
            _childNames.value = currentList
        } else if (currentList.size <= 1) {
            _message.value = "Phải có ít nhất một danh mục con"
        }
    }

    fun save() {
        if (_parentName.value.isBlank()) {
            _message.value = "Vui lòng nhập tên danh mục cha"
            return
        }
        if (_childNames.value.any { it.isBlank() }) {
            _message.value = "Vui lòng nhập tên cho tất cả danh mục con"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = AddCategoryRequest(
                    name = _parentName.value,
                    childrenNames = _childNames.value
                )
                val response = apiService.addCategory(request)
                if (response.isSuccessful) {
                    _message.value = "Thêm danh mục thành công"
                    _isSaved.value = true
                } else {
                    _message.value = "Thêm thất bại: ${response.errorBody()?.string()}"
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
