package com.example.smartlibrary.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE
import retrofit2.http.HTTP
import retrofit2.http.PUT

interface ApiService {
    @GET("api/book")
    suspend fun getAllBooks(): List<BookResponse>

    @GET("api/book/v2")
    suspend fun getAllBooksV2(@Query("filter") filter: String): List<BookResponse>

    @GET("api/book/v2/category-parent/{parentId}")
    suspend fun getBooksByParentCategory(
        @Path("parentId") parentId: String,
        @Query("filter") filter: String
    ): List<BookResponse>

    @GET("api/book/v2/category-child/{childId}")
    suspend fun getBooksByChildCategory(
        @Path("childId") childId: String,
        @Query("filter") filter: String
    ): List<BookResponse>

    @GET("api/book/search2")
    suspend fun searchBooks(@Query("query") query: String): List<BookResponse>

    @POST("api/book/suggest")
    suspend fun getSuggestedBooks(@Body request: SuggestRequest): List<BookResponse>

    @GET("api/book/{id}")
    suspend fun getBookById(@Path("id") id: String): BookResponse

    @POST("api/borrow-cards")
    suspend fun borrowBook(@Body request: BorrowRequest): Response<Unit>


    @GET("api/category")
    suspend fun getCategories(): List<CategoryResponse>

    @GET("api/category-child/category/{parentId}")
    suspend fun getCategoryChildren(@Path("parentId") parentId: String): List<CategoryChildResponse>


    @GET("api/cart/{userId}")
    suspend fun getCart(@Path("userId") userId: String): CartResponseWrapper

    @POST("api/cart/{userId}/add/books")
    suspend fun addToCart(
        @Path("userId") userId: String,
        @Body bookIds: List<String>
    ): Response<Unit>

    @HTTP(method = "DELETE", path = "api/cart/{userId}/remove/books", hasBody = true)
    suspend fun removeBooksFromCart(
        @Path("userId") userId: String,
        @Body bookIds: List<String>
    ): Response<Unit>

    @GET("api/notification/{userId}")
    suspend fun getNotifications(@Path("userId") userId: String): List<NotificationItem>

    @PUT("api/notification/mark-as-read/{id}")
    suspend fun markNotificationAsRead(@Path("id") id: String): Response<Unit>

    @POST("api/chat")
    suspend fun sendChatMessage(@Body request: ChatRequest): ChatResponse
}

data class BookResponse(
    val maSach: String,
    val tenSach: String,
    val tenTacGia: String?,
    val nxb: String?,
    val nam: Int?,
    val hinhAnh: List<String>?,
    val tongSoLuong: Int,
    val soLuongMuon: Int = 0,
    val soLuongXoa: Int = 0,
    val trangThai: String? = null,
    val trongLuong: Int? = null,
    val donGia: Double? = null,
    val moTa: String? = null,
    val categoryChildId: String? = null,
    val categoryChildName: String? = null,
    val categoryParentName: String? = null
)

data class CategoryResponse(
    val id: String,
    val name: String,
    val soLuongDanhMuc: Int?
)

data class CategoryChildResponse(
    val id: String,
    val name: String,
    val parentId: Long? = null,
    val parentName: String? = null
)

data class SuggestRequest(
    val userId: String?,
    val keywords: List<String>
)

data class BorrowRequest(
    val userId: String,
    val bookIds: List<Int>
)



data class CartResponseWrapper(
    val data: List<BookResponse>?
)

data class CartItem(
    val bookId: String
)

data class NotificationItem(
    val id: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean
)

data class ChatRequest(
    val userId: String,
    val message: String
)

data class ChatResponse(
    val reply: String,
    val status: String = "success"
)
