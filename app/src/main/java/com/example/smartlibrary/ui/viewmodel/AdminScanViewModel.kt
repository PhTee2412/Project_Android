package com.example.smartlibrary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartlibrary.network.ApiService
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.network.BorrowCardResponse
import com.example.smartlibrary.network.User
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookWithStatus(
    val book: BookResponse,
    var childId: String? = null,      // ID của sách con – được điền khi quét
    var checked: Boolean = false
)

data class AdminScanState(
    val userIdInput: String = "",
    val selectedUser: User? = null,
    val borrowCards: List<BorrowCardResponse> = emptyList(),
    val selectedCard: BorrowCardResponse? = null,
    val booksInCard: List<BookWithStatus> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val barcodesToBorrow: List<String> = emptyList() // Barcodes scanned for borrowing
)

class AdminScanViewModel(private val apiService: ApiService) : ViewModel() {
    private val _state = MutableStateFlow(AdminScanState())
    val state: StateFlow<AdminScanState> = _state.asStateFlow()

    fun onUserIdInputChange(newValue: String) {
        _state.value = _state.value.copy(userIdInput = newValue)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    fun findUser(userId: String? = null) {
        val id = userId ?: _state.value.userIdInput
        if (id.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val user = apiService.getUserProfile(id)
                _state.value = _state.value.copy(selectedUser = user, userIdInput = "")
                loadBorrowCards(id)
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Không tìm thấy người dùng", isLoading = false)
            }
        }
    }

    private suspend fun loadBorrowCards(userId: String) {
        try {
            val cards = apiService.getBorrowCardsByUser(userId)
            _state.value = _state.value.copy(borrowCards = cards, isLoading = false)
        } catch (e: Exception) {
            _state.value = _state.value.copy(message = "Lỗi khi tải phiếu mượn", isLoading = false)
        }
    }

    fun selectCard(card: BorrowCardResponse?) {
        _state.value = _state.value.copy(selectedCard = card, barcodesToBorrow = emptyList(), booksInCard = emptyList())
        if (card != null) {
            loadBooksForCard(card)
        }
    }

    private fun loadBooksForCard(card: BorrowCardResponse) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val detail = try {
                    apiService.getBorrowCardById(card.id.toString())
                } catch (e: Exception) {
                    null
                }

                val briefs = detail?.bookIds ?: card.borrowedBooks ?: emptyList()

                val books = briefs.map { brief ->
                    async {
                        val bookDetail = apiService.getBookById(brief.bookId.toString())
                        BookWithStatus(
                            book = bookDetail,
                            childId = brief.childBookId,   // có thể null
                            checked = false
                        )
                    }
                }.awaitAll()

                Log.d("AdminScan", "=== Loaded ${books.size} books for card ${card.id} ===")
                books.forEachIndexed { index, bookWithStatus ->
                    Log.d("AdminScan", "[$index] Book: ${bookWithStatus.book.tenSach}, childId=${bookWithStatus.childId}")
                }

                _state.value = _state.value.copy(booksInCard = books, isLoading = false)
            } catch (e: Exception) {
                Log.e("AdminScanViewModel", "Error loading books", e)
                _state.value = _state.value.copy(message = "Lỗi khi tải thông tin sách", isLoading = false)
            }
        }
    }

    fun onBarcodeScanned(barcode: String) {
        val currentState = _state.value
        val card = currentState.selectedCard

        if (card == null) {
            findUser(barcode)
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val childBook = try {
                    apiService.getChildBookByBarcode(barcode)
                } catch (e: Exception) {
                    Log.e("AdminScan", "Error getting child book by barcode", e)
                    null
                }

                Log.d("AdminScan", "Scanned barcode: $barcode, childBook: id=${childBook?.id}, barcode=${childBook?.barcode}, bookId=${childBook?.bookId}")

                _state.update { state ->
                    val books = state.booksInCard.toMutableList()
                    val status = state.selectedCard?.status

                    val bookIndex = when (status) {
                        "Đã yêu cầu" -> {
                            // MƯỢN: tìm theo mã sách cha (maSach)
                            val parentId = childBook?.bookId
                            if (parentId != null) {
                                books.indexOfFirst { it.book.maSach == parentId && !it.checked }
                            } else -1
                        }
                        else -> {
                            // TRẢ: tìm theo mã sách cha, sau đó gán childId
                            val parentId = childBook?.bookId
                            if (parentId != null) {
                                books.indexOfFirst { it.book.maSach == parentId && !it.checked }
                            } else -1
                        }
                    }

                    if (bookIndex != -1) {
                        val book = books[bookIndex]
                        // Nếu là trả sách và childId đang null, ta gán childId từ sách con vừa quét
                        if (status != "Đã yêu cầu" && book.childId == null && childBook?.id != null) {
                            books[bookIndex] = book.copy(childId = childBook.id, checked = true)
                            Log.d("AdminScan", "Đã gán childId=${childBook.id} cho sách ${book.book.tenSach}")
                        } else {
                            books[bookIndex] = book.copy(checked = true)
                        }
                        state.copy(
                            booksInCard = books,
                            barcodesToBorrow = if (status == "Đã yêu cầu") state.barcodesToBorrow + barcode else state.barcodesToBorrow,
                            isLoading = false,
                            message = "Đã xác nhận: ${books[bookIndex].book.tenSach}"
                        )
                    } else {
                        val alreadyChecked = when (status) {
                            "Đã yêu cầu" -> {
                                val parentId = childBook?.bookId
                                parentId != null && books.any { it.book.maSach == parentId && it.checked }
                            }
                            else -> {
                                val parentId = childBook?.bookId
                                parentId != null && books.any { it.book.maSach == parentId && it.checked }
                            }
                        }
                        Log.w("AdminScan", "Không tìm thấy sách phù hợp. alreadyChecked=$alreadyChecked")
                        state.copy(
                            isLoading = false,
                            message = if (alreadyChecked) "Sách này đã được quét rồi"
                            else "Mã sách không khớp với bất kỳ sách nào trong phiếu này"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminScanViewModel", "Barcode scan error", e)
                _state.update { it.copy(isLoading = false, message = "Lỗi khi xử lý mã: $barcode") }
            }
        }
    }

    fun submitTransaction() {
        val currentState = _state.value
        val card = currentState.selectedCard ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                if (card.status == "Đã yêu cầu") {
                    if (currentState.barcodesToBorrow.isNotEmpty()) {
                        apiService.borrowBooksConfirm(card.id, currentState.barcodesToBorrow)
                    }
                } else {
                    val checkedBooks = currentState.booksInCard.filter { it.checked }
                    checkedBooks.forEach { book ->
                        book.childId?.let { code ->
                            Log.d("AdminScan", "Trả sách với childId: $code")
                            apiService.returnOneBook(card.id, mapOf("barcode" to code))
                        }
                    }
                }
                _state.value = _state.value.copy(message = "Hoàn tất thành công!")
                selectCard(null)
                findUser(currentState.selectedUser?.id.toString())
            } catch (e: Exception) {
                Log.e("AdminScanViewModel", "Submit error", e)
                _state.value = _state.value.copy(message = "Lỗi: ${e.message}", isLoading = false)
            }
        }
    }

    fun goBack() {
        _state.value = _state.value.copy(
            selectedUser = null,
            borrowCards = emptyList(),
            selectedCard = null,
            booksInCard = emptyList(),
            userIdInput = ""
        )
    }
}