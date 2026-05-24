package com.example.smartlibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminUserListViewModel(private val apiService: ApiService) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val _filteredUsers = MutableStateFlow<List<User>>(emptyList())
    val filteredUsers = _filteredUsers.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages = _totalPages.asStateFlow()

    private val itemsPerPage = 10

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getAdminUsers()
                if (response.isSuccessful) {
                    val userList = response.body()?.data ?: emptyList()
                    _users.value = userList
                    filterUsers()
                } else {
                    _message.value = "Không thể tải danh sách người dùng"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        filterUsers()
    }

    private fun filterUsers() {
        val query = _searchQuery.value.lowercase()
        val filtered = if (query.isEmpty()) {
            _users.value
        } else {
            _users.value.filter {
                it.id.toString().contains(query) ||
                        it.username?.lowercase()?.contains(query) == true ||
                        it.email?.lowercase()?.contains(query) == true ||
                        it.phone?.contains(query) == true
            }
        }
        _filteredUsers.value = filtered
        _totalPages.value = kotlin.math.ceil(filtered.size.toDouble() / itemsPerPage).toInt().coerceAtLeast(1)
        _currentPage.value = 1
    }

    fun setPage(page: Int) {
        if (page in 1.._totalPages.value) {
            _currentPage.value = page
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteAdminUser(userId.toInt())
                if (response.isSuccessful) {
                    _message.value = "Xóa người dùng thành công"
                    loadUsers()
                } else {
                    _message.value = "Xóa thất bại"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun getCurrentPageUsers(): List<User> {
        val start = (_currentPage.value - 1) * itemsPerPage
        val end = (start + itemsPerPage).coerceAtMost(_filteredUsers.value.size)
        return if (start < _filteredUsers.value.size) {
            _filteredUsers.value.subList(start, end)
        } else {
            emptyList()
        }
    }
}
