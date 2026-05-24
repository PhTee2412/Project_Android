package com.example.smartlibrary.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.network.CategoryChildResponse
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

class AdminEditBookViewModel(
    private val apiService: ApiService,
    private val bookId: Long
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    // Form states
    private val _bookName = MutableStateFlow("")
    val bookName = _bookName.asStateFlow()

    private val _author = MutableStateFlow("")
    val author = _author.asStateFlow()

    private val _publisher = MutableStateFlow("")
    val publisher = _publisher.asStateFlow()

    private val _year = MutableStateFlow("")
    val year = _year.asStateFlow()

    private val _quantityAdded = MutableStateFlow("") // Số lượng sách thêm vào
    val quantityAdded = _quantityAdded.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _weight = MutableStateFlow("")
    val weight = _weight.asStateFlow()

    private val _price = MutableStateFlow("")
    val price = _price.asStateFlow()

    private val _status = MutableStateFlow("")
    val status = _status.asStateFlow()

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri = _imageUri.asStateFlow()

    private val _imageUrl = MutableStateFlow<String?>(null) // Ảnh hiện tại từ server
    val imageUrl = _imageUrl.asStateFlow()

    // Categories
    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CategoryResponse?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedSubCategory = MutableStateFlow<CategoryChildResponse?>(null)
    val selectedSubCategory = _selectedSubCategory.asStateFlow()

    private var originalBook: BookResponse? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Tải danh mục trước
                val cats = apiService.getCategories()
                _categories.value = cats

                // Tải thông tin sách
                val book = apiService.getBookById(bookId.toString())
                originalBook = book
                
                // Điền dữ liệu vào form
                _bookName.value = book.tenSach
                _author.value = book.tenTacGia ?: ""
                _publisher.value = book.nxb ?: ""
                _year.value = book.nam?.toString() ?: ""
                _description.value = book.moTa ?: ""
                _weight.value = book.trongLuong?.toString() ?: ""
                _price.value = book.donGia?.toInt()?.toString() ?: ""
                _status.value = book.trangThai ?: ""
                _imageUrl.value = book.hinhAnh?.firstOrNull()

                // Tìm category và subcategory tương ứng
                if (book.categoryChildId != null) {
                    cats.forEach { cat ->
                        val sub = cat.children?.find { it.id == book.categoryChildId }
                        if (sub != null) {
                            _selectedCategory.value = cat
                            _selectedSubCategory.value = sub
                        }
                    }
                }
            } catch (e: Exception) {
                _message.value = "Lỗi tải dữ liệu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onBookNameChange(value: String) { _bookName.value = value }
    fun onAuthorChange(value: String) { _author.value = value }
    fun onPublisherChange(value: String) { _publisher.value = value }
    fun onYearChange(value: String) { _year.value = value }
    fun onQuantityAddedChange(value: String) { _quantityAdded.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }
    fun onWeightChange(value: String) { _weight.value = value }
    fun onPriceChange(value: String) { _price.value = value }
    fun onImageUriChange(uri: Uri?) { _imageUri.value = uri }

    fun onCategorySelected(category: CategoryResponse) {
        if (_selectedCategory.value?.id != category.id) {
            _selectedCategory.value = category
            _selectedSubCategory.value = null
        }
    }

    fun onSubCategorySelected(sub: CategoryChildResponse) {
        _selectedSubCategory.value = sub
    }

    fun clearMessage() { _message.value = null }

    fun submit(context: Context, onBookUpdated: (Long) -> Unit) {
        if (!validate()) return

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val updates = mutableMapOf<String, Any>()
                val initial = originalBook ?: throw Exception("Không có dữ liệu gốc")

                // Nếu sách đã xóa, chỉ cho phép cập nhật số lượng để phục hồi
                if (initial.trangThai == "DA_XOA") {
                    val addQty = _quantityAdded.value.toIntOrNull() ?: 0
                    if (addQty <= 0) {
                        _message.value = "Vui lòng nhập số lượng > 0 để phục hồi sách"
                        _isSubmitting.value = false
                        return@launch
                    }
                    updates["tongSoLuong"] = addQty
                    val response = apiService.updateBook(bookId, updates)
                    if (response.isSuccessful) {
                        _message.value = "Sách đã được phục hồi thành công"
                        onBookUpdated(bookId)
                    } else {
                        _message.value = "Lỗi: ${response.errorBody()?.string()}"
                    }
                    return@launch
                }

                // Logic so sánh thay đổi cho sách bình thường
                if (_bookName.value != initial.tenSach) updates["tenSach"] = _bookName.value
                if (_author.value != (initial.tenTacGia ?: "")) updates["tenTacGia"] = _author.value
                if (_publisher.value != (initial.nxb ?: "")) updates["nxb"] = _publisher.value
                val yearInt = _year.value.toIntOrNull() ?: 0
                if (yearInt != (initial.nam ?: 0)) updates["nam"] = yearInt
                val weightInt = _weight.value.toIntOrNull() ?: 0
                if (weightInt != (initial.trongLuong ?: 0)) updates["trongLuong"] = weightInt
                val priceInt = _price.value.toIntOrNull() ?: 0
                if (priceInt != (initial.donGia?.toInt() ?: 0)) updates["donGia"] = priceInt
                if (_description.value != (initial.moTa ?: "")) updates["moTa"] = _description.value
                
                if (_selectedSubCategory.value?.id != initial.categoryChildId) {
                    updates["categoryChildId"] = _selectedSubCategory.value?.id ?: ""
                }

                val addQty = _quantityAdded.value.toIntOrNull() ?: 0
                updates["tongSoLuong"] = addQty // Backend xử lý cộng thêm

                // Xử lý ảnh mới
                if (_imageUri.value != null) {
                    val file = getFileFromUri(context, _imageUri.value!!)
                    if (file != null) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("files", file.name, requestFile)
                        val uploadResponse = apiService.uploadImage(listOf(body))
                        if (uploadResponse.isSuccessful) {
                            val urls = uploadResponse.body()
                            if (!urls.isNullOrEmpty()) {
                                updates["hinhAnh"] = urls
                            }
                        } else {
                            throw Exception("Upload ảnh thất bại")
                        }
                    }
                }

                if (updates.isEmpty()) {
                    _message.value = "Không có thay đổi nào"
                    _isSubmitting.value = false
                    return@launch
                }

                val response = apiService.updateBook(bookId, updates)
                if (response.isSuccessful) {
                    _message.value = "Cập nhật sách thành công"
                    onBookUpdated(bookId)
                } else {
                    _message.value = "Lỗi: ${response.errorBody()?.string()}"
                }

            } catch (e: Exception) {
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    private fun validate(): Boolean {
        if (_status.value == "DA_XOA") {
            if (_quantityAdded.value.isBlank()) {
                _message.value = "Vui lòng nhập số lượng để phục hồi"
                return false
            }
            return true
        }

        if (_bookName.value.isBlank() || _author.value.isBlank() || _year.value.isBlank() ||
            _publisher.value.isBlank() || _description.value.isBlank() ||
            _selectedSubCategory.value == null || _weight.value.isBlank() || _price.value.isBlank()
        ) {
            _message.value = "Vui lòng điền đầy đủ thông tin"
            return false
        }
        return true
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "edit_book_image_${System.currentTimeMillis()}.jpg")
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
