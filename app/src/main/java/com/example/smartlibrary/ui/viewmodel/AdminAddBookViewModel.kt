package com.example.smartlibrary.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.AddBookRequest
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookPayload
import com.example.smartlibrary.network.CategoryChildPayload
import com.example.smartlibrary.network.CategoryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class AdminAddBookViewModel(private val apiService: ApiService) : ViewModel() {

    private val _bookName = MutableStateFlow("")
    val bookName = _bookName.asStateFlow()

    private val _author = MutableStateFlow("")
    val author = _author.asStateFlow()

    private val _publisher = MutableStateFlow("")
    val publisher = _publisher.asStateFlow()

    private val _year = MutableStateFlow("")
    val year = _year.asStateFlow()

    private val _quantity = MutableStateFlow("")
    val quantity = _quantity.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _weight = MutableStateFlow("")
    val weight = _weight.asStateFlow()

    private val _price = MutableStateFlow("")
    val price = _price.asStateFlow()

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri = _imageUri.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CategoryResponse?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedSubCategoryId = MutableStateFlow("")
    val selectedSubCategoryId = _selectedSubCategoryId.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val response = apiService.getCategories()
                _categories.value = response
            } catch (e: Exception) {
                _message.value = "Không thể tải danh mục: ${e.message}"
            }
        }
    }

    fun onBookNameChange(value: String) { _bookName.value = value }
    fun onAuthorChange(value: String) { _author.value = value }
    fun onPublisherChange(value: String) { _publisher.value = value }
    fun onYearChange(value: String) { _year.value = value }
    fun onQuantityChange(value: String) { _quantity.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }
    fun onWeightChange(value: String) { _weight.value = value }
    fun onPriceChange(value: String) { _price.value = value }
    fun onImageUriChange(uri: Uri?) { _imageUri.value = uri }

    fun onCategorySelected(category: CategoryResponse) {
        _selectedCategory.value = category
        _selectedSubCategoryId.value = "" // Reset subcategory
    }

    fun onSubCategorySelected(subCategoryId: String) {
        _selectedSubCategoryId.value = subCategoryId
    }

    fun clearMessage() { _message.value = null }

    fun submit(context: Context, onBookAdded: (Long) -> Unit) {
        if (!validate()) return

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                var finalImageUrl = ""
                // 1. Upload ảnh nếu có
                _imageUri.value?.let { uri ->
                    val file = getFileFromUri(context, uri)
                    if (file != null) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("files", file.name, requestFile)
                        val uploadResponse = apiService.uploadImage(listOf(body))
                        if (uploadResponse.isSuccessful) {
                            // The apiService.uploadImage returns Response<List<String>>, so we access the list directly.
                            finalImageUrl = uploadResponse.body()?.firstOrNull() ?: ""
                        } else {
                            throw Exception("Upload ảnh thất bại")
                        }
                    }
                }

                // 2. Tạo request thêm sách
                val request = AddBookRequest(
                    book = BookPayload(
                        tenSach = _bookName.value,
                        moTa = _description.value,
                        tenTacGia = _author.value,
                        nxb = _publisher.value,
                        nam = _year.value.toIntOrNull() ?: 0,
                        hinhAnh = if (finalImageUrl.isNotEmpty()) listOf(finalImageUrl) else emptyList(),
                        trongLuong = _weight.value.toIntOrNull() ?: 0,
                        donGia = _price.value.toIntOrNull() ?: 0,
                        categoryChild = CategoryChildPayload(id = _selectedSubCategoryId.value)
                    ),
                    quantity = _quantity.value.toIntOrNull() ?: 0
                )

                val response = apiService.addBook(request)
                if (response.isSuccessful) {
                    val bookResponse = response.body()
                    if (bookResponse != null) {
                        _message.value = "Thêm sách thành công!"
                        onBookAdded(bookResponse.maSach)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _message.value = "Lỗi: $errorBody"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    private fun validate(): Boolean {
        if (_bookName.value.isBlank() || _author.value.isBlank() || _year.value.isBlank() ||
            _publisher.value.isBlank() || _quantity.value.isBlank() || _description.value.isBlank() ||
            _selectedSubCategoryId.value.isBlank() || _weight.value.isBlank() || _price.value.isBlank()
        ) {
            _message.value = "Vui lòng điền đầy đủ thông tin"
            return false
        }
        if ((_year.value.toIntOrNull() ?: 0) <= 0) {
            _message.value = "Năm xuất bản không hợp lệ"
            return false
        }
        if ((_quantity.value.toIntOrNull() ?: 0) < 1) {
            _message.value = "Số lượng phải lớn hơn 0"
            return false
        }
        return true
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}
