package com.example.smartlibrary.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("api/book")
    suspend fun getAllBooks(): List<BookResponse>

    @GET("api/book/search2")
    suspend fun searchBooks(@Query("query") query: String): List<BookResponse>

    @POST("api/book/suggest")
    suspend fun getSuggestedBooks(@Body request: SuggestRequest): List<BookResponse>
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
    val trangThai: String? = null
)

data class SuggestRequest(
    val userId: String?,
    val keywords: List<String>
)
