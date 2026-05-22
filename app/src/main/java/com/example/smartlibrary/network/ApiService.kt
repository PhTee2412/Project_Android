package com.example.smartlibrary.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*


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

    @GET("api/notification/{userId}")
    suspend fun getNotifications(@Path("userId") userId: String): List<NotificationItem>

    @PUT("api/notification/mark-as-read/{id}")
    suspend fun markNotificationAsRead(@Path("id") id: Long): Response<Unit>

    // Settings
    @GET("api/settings")
    suspend fun getSettings(): Setting

    // Cart
    @GET("api/cart/{userId}")
    suspend fun getCart(@Path("userId") userId: String): CartResponseWrapper

    @POST("api/cart/{userId}/add/books")
    suspend fun addToCart(
        @Path("userId") userId: String,
        @Body bookIds: List<Long>
    ): Response<Unit>

    @HTTP(method = "DELETE", path = "api/cart/{userId}/remove/books", hasBody = true)
    suspend fun removeBooksFromCart(
        @Path("userId") userId: String,
        @Body bookIds: List<Long>
    ): Response<Unit>

    @POST("api/chat/message")
    suspend fun sendChatMessage(@Body request: ChatRequest): ChatResponse

    // --- Profile API ---
    @GET("api/user/{id}")
    suspend fun getUserProfile(@Path("id") id: String): User

    @PUT("api/user/{id}")
    suspend fun updateUserProfile(@Path("id") id: String, @Body updates: Map<String, String?>): Response<UserProfileResponse>

    @Multipart
    @POST("api/user/upload-avatar")
    suspend fun uploadAvatar(
        @Part("id") id: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<AvatarResponse>

    @POST("api/user/verify-email-update")
    suspend fun verifyEmailUpdate(@Body body: VerifyOtpRequest): Response<UserProfileResponse>

    @PUT("api/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<ChangePasswordResponse>

    // --- Borrow Cards API ---
    @GET("api/borrow-cards/user/{userId}")
    suspend fun getBorrowCardsByUser(@Path("userId") userId: String): List<BorrowCardResponse>

    @GET("api/borrow-cards/{id}")
    suspend fun getBorrowCardById(@Path("id") id: String): BorrowCardDetailResponse

    @DELETE("api/borrow-cards/{id}")
    suspend fun deleteBorrowCard(@Path("id") id: String): Response<Unit>

    // --- Fines API ---
    @GET("api/fines/{userId}")
    suspend fun getFinesByUser(@Path("userId") userId: String): List<FineResponse>

    @GET("api/fine/{id}")
    suspend fun getFineById(@Path("id") id: String): FineDetailResponse

    @POST("api/fine/pay-momo/{id}")
    suspend fun payFineByMomo(@Path("id") id: String): MomoPaymentResponse

    @POST("api/fine/payment/confirm")
    suspend fun confirmFinePayment(@Body body: ConfirmPaymentRequest): Response<Unit>

    // --- Auth API ---
    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("api/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("api/register/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): VerifyOtpResponse

    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body body: SocialLoginRequest): LoginResponse

    @POST("api/auth/facebook")
    suspend fun loginWithFacebook(@Body body: SocialLoginRequest): LoginResponse
}

data class SocialLoginRequest(val token: String)

data class BookResponse(
    val maSach: Long,
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

data class NotificationItem(
    val id: Long,
    val message: String,
    val timestamp: String,
    val isRead: Boolean
)

data class Setting(
    val id: Long? = null,
    val finePerDay: Int = 0,
    val waitingToTake: Int = 0,
    val borrowDay: Int = 0,
    val startToMail: Int = 0,
    val maxBorrowedBooks: Int? = null
)

data class CartResponseWrapper(
    val message: String? = null,
    val data: List<CartItemDTO>?
)

data class CartItemDTO(
    val bookId: Long,          // Backend trả về bookId (Long)
    val tenSach: String,
    val tenTacGia: String?,
    val nxb: String?,
    val nam: Int?,
    val hinhAnh: List<String>?,
    val tongSoLuong: Int,
    val soLuongMuon: Int,
    val soLuongXoa: Int,
    val trangThai: String?
)

data class ChatRequest(
    val userId: String,
    val message: String
)

data class ChatResponse(
    val reply: String,
    val status: String = "success"
)

// --- User Profile Responses ---
data class UserProfileResponse(
    val status: String? = null,
    val message: String? = null,
    val data: UserDataResponse
)

data class UserDataResponse(
    val id: Int?,
    val fullname: String?,
    val email: String?,
    val username: String? = null,
    val phone: String?,
    val birthdate: String?,
    val joined_date: String?,
    val avatar_url: String?
)

data class AvatarResponse(
    val status: String?,
    val message: String?,
    val data: AvatarData
)

data class AvatarData(val avatar_url: String?)

data class VerifyOtpRequest(
    val id: String? = null,
    val email: String,
    val otp: String
)

data class ChangePasswordRequest(
    val id: String,
    val oldPassword: String,
    val newPassword: String
)

data class ChangePasswordResponse(
    val message: String? = null,
    val status: String? = null
)

// --- Borrow Card Data Classes ---
data class BorrowCardResponse(
    val id: Int,
    val userId: Int,
    val borrowDate: String?,
    val dueDate: String?,
    val getBookDate: String?,
    val status: String?,
    val soNgayTre: Int?,
    val bookIds: List<BorrowedBookBrief>? = null
)

data class BorrowedBookBrief(
    val bookId: Int,
    val childBookId: String? = null,
    val name: String?,
    val author: String?,
    val image: String?,
    val category: String?,
    val publisher: String?,
    val borrowCount: Int? = null
)

data class BorrowCardDetailResponse(
    val id: Int,
    val userId: Int,
    val userName: String?,
    val borrowDate: String?,
    val dueDate: String?,
    val getBookDate: String?,
    val status: String?,
    val totalBooks: Int?,
    val bookIds: List<BorrowedBookBrief>?
)

// --- Fines Data Classes ---
data class FineResponse(
    val id: Int,
    val userId: Int? = null,
    val soTien: Double? = null,
    val noiDung: String? = null,
    val trangThai: String? = null,
    val ngayThanhToan: String? = null
)

data class UserBrief(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null
)

data class FineDetailResponse(
    val id: Int,
    val userId: Int? = null, // Đổi từ UserBrief? thành Int? để khớp với giá trị NUMBER từ Backend
    val soTien: Double? = null,
    val noiDung: String? = null,
    val trangThai: String? = null,
    val ngayThanhToan: String? = null,
    val cardId: String? = null,
    val borrowCard: BorrowCardInFine? = null,
    val tenND: String? = null
)

data class BorrowCardInFine(
    val id: Int,
    val borrowedBooks: List<BorrowedBookBrief>?
)

data class MomoPaymentResponse(val payUrl: String, val status: String?)
data class ConfirmPaymentRequest(val orderId: String, val amount: String)

// --- Auth API ---
data class LoginRequest(
    val email: String? = null,
    val phone: String? = null,
    val password: String
)

data class LoginResponse(
    val status: String? = null,
    val message: String? = null,
    val data: LoginData? = null
)

data class LoginData(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: UserBrief? = null
)

data class RegisterRequest(
    val username: String,
    val email: String? = null,
    val phone: String? = null,
    val password: String,
    val birthdate: String? = null,
    val gender: String? = null
)

data class RegisterResponse(
    val status: String? = null,
    val message: String? = null
)

data class VerifyOtpResponse(
    val status: String? = null,
    val message: String? = null
)

data class SettingsResponse(
    val status: String? = null,
    val message: String? = null,
    val data: SettingsData? = null
)
data class User(
    val id: Long,
    val username: String?,
    val email: String?,
    val fullname: String?,
    val phone: String?,
    val role: String?,
    val gender: String?,
    val avatar_url: String?,
    val birthdate: String?,
    val joined_date: String?
)

data class SettingsData(val maxBorrowedBooks: Int? = null)
