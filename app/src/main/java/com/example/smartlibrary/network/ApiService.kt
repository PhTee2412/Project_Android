package com.example.smartlibrary.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement
import com.google.gson.Gson

interface ApiService {
    // ==================== BOOK APIs ====================
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

    @POST("api/book")
    suspend fun addBook(@Body payload: AddBookRequest): Response<BookResponse>

    @PATCH("api/book/{id}")
    suspend fun updateBook(@Path("id") id: Long, @Body updates: Map<String, @JvmSuppressWildcards Any?>): Response<Unit>

    @Multipart
    @POST("api/upload/image")
    suspend fun uploadImage(@Part files: List<MultipartBody.Part>): Response<List<String>>

    // ==================== CHILD BOOK APIs ====================
    @GET("api/bookchild/book/{bookId}")
    suspend fun getChildBooks(@Path("bookId") bookId: Long): List<ChildBookResponse>

    @POST("api/bookchild/book/{bookId}/add")
    suspend fun addChildBook(@Path("bookId") bookId: Long): Response<ChildBookResponse>

    @DELETE("api/bookchild/{childId}")
    suspend fun deleteChildBook(@Path("childId") childId: String): Response<Unit>

    @GET("api/bookchild/{id}")
    suspend fun getChildBookById(@Path("id") id: String): BookResponse

    @GET("api/bookchild/barcode/{barcode}")
    suspend fun getChildBookByBarcode(@Path("barcode") barcode: String): BookChildFullResponse

    // ==================== CATEGORY APIs ====================
    @GET("api/category")
    suspend fun getCategories(): List<CategoryResponse>

    @POST("api/category")
    suspend fun addCategory(@Body body: AddCategoryRequest): Response<CategoryResponse>

    @GET("api/category/{id}")
    suspend fun getCategoryById(@Path("id") id: String): CategoryResponse

    @PATCH("api/category/{id}")
    suspend fun updateCategory(@Path("id") id: String, @Body body: Map<String, String>): Response<Unit>

    @DELETE("api/category/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<Unit>

    @POST("api/category-child/category/{parentId}/add")
    suspend fun addChildCategory(@Path("parentId") parentId: String, @Body body: Map<String, String>): Response<CategoryChildResponse>

    @GET("api/category-child/{id}")
    suspend fun getChildCategoryById(@Path("id") id: String): CategoryChildResponse

    @PATCH("api/category-child/{id}")
    suspend fun updateChildCategory(@Path("id") id: String, @Body body: Map<String, String>): Response<Unit>

    @DELETE("api/category-child/{id}")
    suspend fun deleteChildCategory(@Path("id") id: String): Response<Unit>

    @GET("api/category-child/category/{parentId}")
    suspend fun getCategoryChildren(@Path("parentId") parentId: String): List<CategoryChildResponse>

    // ==================== NOTIFICATION APIs ====================
    @GET("api/notification/{userId}")
    suspend fun getNotifications(@Path("userId") userId: String): List<NotificationItem>

    @PUT("api/notification/mark-as-read/{id}")
    suspend fun markNotificationAsRead(@Path("id") id: Long): Response<Unit>

    // ==================== SETTINGS APIs ====================
    @GET("api/settings")
    suspend fun getSettings(): Setting

    @POST("api/settings")
    suspend fun updateSettings(@Body settings: Setting): Response<Setting>

    // ==================== CART APIs ====================
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

    // ==================== CHAT APIs ====================
    @POST("api/chat/message")
    suspend fun sendChatMessage(@Body request: ChatRequest): ChatResponse

    // ==================== PROFILE APIs ====================
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

    // ==================== BORROW CARD APIs ====================
    @POST("api/borrow-cards")
    suspend fun borrowBook(@Body request: BorrowRequest): Response<Unit>

    // Lấy danh sách phiếu mượn của user (Backend dùng POST)
    @POST("api/borrow-cards/user/{userId}")
    suspend fun getBorrowCardsByUser(@Path("userId") userId: String): List<BorrowCardResponse>

    @GET("api/borrow-cards/{id}")
    suspend fun getBorrowCardById(@Path("id") id: String): BorrowCardDetailResponse

    @DELETE("api/borrow-cards/{id}")
    suspend fun deleteBorrowCard(@Path("id") id: String): Response<Unit>

    @GET("api/borrow-cards")
    suspend fun getAllBorrowCards(): List<BorrowCardResponse>

    @POST("api/borrow-cards")
    suspend fun createBorrowCard(@Body request: CreateBorrowCardRequest): Response<BorrowCardResponse>

    @PUT("api/borrow-cards/expired/{id}")
    suspend fun markExpired(@Path("id") id: String): Response<Unit>

    @POST("api/borrow-cards/askToReturn")
    suspend fun askToReturn(@Body list: List<BorrowCardResponse>): Response<Unit>

    @PUT("api/borrow-cards/borrow/{id}")
    suspend fun borrowBooksConfirm(@Path("id") id: Int, @Body barcodes: List<String>): Response<Unit>

    @PUT("api/borrow-cards/return-one/{cardId}")
    suspend fun returnOneBook(@Path("cardId") cardId: Int, @Body body: Map<String, String>): Response<Unit>

    // ==================== UPLOAD APIs ====================
    @Multipart
    @POST("api/upload/barcodeImage")
    suspend fun uploadBarcodeImage(
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody
    ): Response<BarcodeUploadResponse>

    // ==================== FINE APIs ====================
    @GET("api/fines")
    suspend fun getAllFines(): List<FineResponse>

    @GET("api/fines/{userId}")
    suspend fun getFinesByUser(@Path("userId") userId: String): List<FineResponse>

    @GET("api/fine/{id}")
    suspend fun getFineById(@Path("id") id: String): FineDetailResponse

    @POST("api/addFine")
    suspend fun addFine(@Body payload: AddFineRequest): Response<FineResponse>

    @PUT("api/fine/pay/{id}")
    suspend fun payFine(@Path("id") id: String): Response<Unit>

    @POST("api/fine/pay-momo/{id}")
    suspend fun payFineByMomo(@Path("id") id: String): MomoPaymentResponse

    @POST("api/fine/payment/confirm")
    suspend fun confirmFinePayment(@Body body: ConfirmPaymentRequest): Response<Unit>

    // ==================== AUTH APIs ====================
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

    @POST("api/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<Unit>

    @POST("api/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<Unit>


    // Endpoint login admin – dùng chung endpoint /api/login nhưng request khác
    @POST("api/login")
    suspend fun adminLogin(@Body body: AdminLoginRequest): AdminLoginResponse

    // Endpoint verify OTP admin
    @POST("api/admin/verify-otp")
    suspend fun verifyAdminOtp(@Body body: VerifyOtpRequest): AdminLoginResponse


    @GET("api/book/dashboard")
    suspend fun getDashboard(): DashboardResponse

    @GET("api/book/search")
    suspend fun searchBooksAdmin(
        @Query("all") all: String? = null,
        @Query("title") title: String? = null,
        @Query("author") author: String? = null,
        @Query("category") category: String? = null,
        @Query("publisher") publisher: String? = null,
        @Query("year") year: String? = null,
        @Query("status") status: String? = null,
        @Query("sortByBorrowCount") sortByBorrowCount: Boolean = false
    ): List<BookResponse>

    @DELETE("api/book/{id}")
    suspend fun deleteBook(@Path("id") id: Long): Response<Unit>

    @GET("api/book/admin/all")
    suspend fun getAllBooksAdmin(): List<BookResponse>

    // ==================== ADMIN USER APIs ====================
    @GET("api/admin/users")
    suspend fun getAdminUsers(): Response<UserListResponse>

    @POST("api/admin/users")
    suspend fun addAdminUser(@Body body: AddUserRequest): Response<AdminUserSingleResponse>

    @PUT("api/admin/users/{id}")
    suspend fun updateAdminUser(@Path("id") id: Int, @Body body: UpdateUserRequest): Response<AdminUserSingleResponse>

    @DELETE("api/admin/users/{id}")
    suspend fun deleteAdminUser(@Path("id") id: Int): Response<Unit>

    @POST("api/admin/verify-otp-create")
    suspend fun verifyOtpCreate(@Body body: VerifyOtpRequest): Response<AdminUserSingleResponse>

    @GET("api/user")
    suspend fun getAllUsers(): List<User>

}

// ==================== DATA CLASSES ====================

data class ForgotPasswordRequest(val emailOrPhone: String)

data class ResetPasswordRequest(
    val emailOrPhone: String,
    val otp: String,
    val newPassword: String
)

data class BarcodeUploadResponse(val result: String?)

data class BookChildFullResponse(
    val id: String,
    val barcode: String?,
    val bookId: Long?,
    val status: String?
)

data class AddFineRequest(
    val userId: Int,
    val soTien: Double,
    val noiDung: String,
    val cardId: Any? = null
)

data class CreateBorrowCardRequest(
    val userId: Long,
    val bookIds: List<Long>,
    val borrowDate: String? = null,
    val dueDate: String? = null
)

data class UserListResponse(
    val message: String?,
    val data: List<User>
)

data class AdminUserSingleResponse(
    val message: String?,
    val data: User?
)

data class AddUserRequest(
    val username: String,
    val email: String,
    val phone: String?,
    val birthdate: String?,
    val avatar_url: String?,
    val role: String,
    val gender: String
)

data class UpdateUserRequest(
    val username: String?,
    val email: String?,
    val phone: String?,
    val birthdate: String?,
    val avatar_url: String?,
    val role: String?,
    val gender: String?
)

data class AddCategoryRequest(
    val name: String,
    val childrenNames: List<String>
)

data class AddBookRequest(
    val book: BookPayload,
    val quantity: Int
)

data class BookPayload(
    val tenSach: String,
    val moTa: String,
    val tenTacGia: String,
    val nxb: String,
    val nam: Int,
    val hinhAnh: List<String>,
    val trongLuong: Int,
    val donGia: Int,
    val categoryChild: CategoryChildPayload,
    val trangThai: String = "CON_SAN"
)

data class CategoryChildPayload(val id: String)

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
    val categoryParentName: String? = null,
    val viTri: String? = null
)

data class ChildBookResponse(
    val id: String,
    val barcode: String? = null,
    val status: String? = null
)

data class CategoryResponse(
    val id: String,
    val name: String,
    val soLuongDanhMuc: Int?,
    val children: List<CategoryChildResponse>? = null
)

data class CategoryChildResponse(
    val id: String,
    val name: String,
    val parentId: String? = null, // Changed to String as it might be UUID or similar string ID
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
    val bookId: Long,
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
    val email: String? = null,
    val phone: String? = null,
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

data class BorrowCardResponse(
    val id: Int,
    val userId: Int,
    val borrowDate: String?,
    val dueDate: String?,
    val getBookDate: String?,
    val status: String?,
    val soNgayTre: Int?,
    val borrowedBooks: List<BorrowedBookBrief>? = null
)

data class BorrowedBookBrief(
    @SerializedName("maSach") val bookId: Int,
    val childBookId: String? = null,
    val name: String?,
    val author: String?,
    val image: String?,
    val category: String?,
    val publisher: String?,
    val viTri: String? = null, // Added for location
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
    @SerializedName("userId")
    val userIdElement: JsonElement? = null,
    val soTien: Double? = null,
    val noiDung: String? = null,
    val trangThai: String? = null,
    val ngayThanhToan: String? = null,
    @SerializedName("cardId")
    val cardIdElement: JsonElement? = null,
    val tenND: String? = null
) {
    val userId: UserBrief?
        get() = try {
            if (userIdElement?.isJsonObject == true) {
                Gson().fromJson(userIdElement, UserBrief::class.java)
            } else if (userIdElement?.isJsonPrimitive == true) {
                UserBrief(id = userIdElement.asInt)
            } else null
        } catch (e: Exception) { null }

    // Trả về object mượn/trả nếu cardId là object có "borrowedBooks"
    val cardId: BorrowCardInFine?
        get() = try {
            if (cardIdElement?.isJsonObject == true) {
                val obj = cardIdElement.asJsonObject
                if (obj.has("borrowedBooks")) {
                    Gson().fromJson(cardIdElement, BorrowCardInFine::class.java)
                } else null
            } else null
        } catch (e: Exception) { null }

    // Trả về sách nếu cardId là object sách (không có "borrowedBooks")
    val bookFromCard: BookResponse?
        get() = try {
            if (cardIdElement?.isJsonObject == true) {
                val obj = cardIdElement.asJsonObject
                if (!obj.has("borrowedBooks")) {
                    Gson().fromJson(cardIdElement, BookResponse::class.java)
                } else null
            } else null
        } catch (e: Exception) { null }

    // Trả về chuỗi nếu cardId là string (dành cho lý do "Khác")
    val cardIdString: String?
        get() = if (cardIdElement?.isJsonPrimitive == true) cardIdElement.asString else null
}

data class BorrowCardInFine(
    val id: Int,
    val borrowedBooks: List<BorrowedBookBrief>?,
    val soNgayTre: Int? = null,
    val getBookDate: String? = null,
    val dueDate: String? = null
)

data class MomoPaymentResponse(val payUrl: String, val status: String?)
data class ConfirmPaymentRequest(val orderId: String, val amount: String)

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

// ==================== ADMIN AUTH (riêng biệt) ====================

data class AdminLoginRequest(
    val email: String? = null,
    val phone: String? = null,
    val password: String,
    val isFEAdmin: Boolean = true
)

data class AdminLoginResponse(
    val status: String? = null,
    val message: String? = null,
    val data: LoginData? = null,
    val email: String? = null   // backend trả về email khi yêu cầu OTP
)

data class DashboardResponse(
    val totalBooks: Long,
    val totalBookQuantity: Long,
    val newBooksThisMonth: Long,
    val borrowedBooksThisMonth: Long,
    val monthlyStats: List<MonthlyStat>,
    val booksToRestock: List<BookResponse>
)

data class MonthlyStat(
    val monthLabel: String,
    val totalBooks: Long,
    val totalBookQuantity: Long,
    val newBooks: Long,
    val borrowedBooks: Long
)
