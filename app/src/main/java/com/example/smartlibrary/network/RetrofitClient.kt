package com.example.smartlibrary.network

import android.content.Context
import android.util.Log
import com.example.smartlibrary.data.SessionManager
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/"
    private var sessionManager: SessionManager? = null
    private var adminSessionManager: SessionManager? = null

    fun initialize(context: Context) {
        sessionManager = SessionManager(context)
    }

    // Hàm để set session admin khi cần
    fun setAdminSession(session: SessionManager?) {
        adminSessionManager = session
    }

    private fun getOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        builder.addInterceptor { chain ->
            val originalRequest = chain.request()
            // Ưu tiên token admin nếu có, sau đó đến token user
            val token = adminSessionManager?.getAccessToken()
                ?: sessionManager?.getAccessToken()
            Log.d("RetrofitClient", "Gửi request tới: ${originalRequest.url}")
            Log.d("RetrofitClient", "Token hiện tại: ${if (token.isNullOrEmpty()) "KHÔNG CÓ TOKEN" else token}")

            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            val newRequest = requestBuilder.build()
            chain.proceed(newRequest)
        }

        return builder.build()
    }

    // Deserializer an toàn, xử lý null cho tất cả trường
    private class BorrowCardResponseDeserializer : JsonDeserializer<BorrowCardResponse> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): BorrowCardResponse {
            val obj = json.asJsonObject
            
            // Hàm helper để lấy giá trị an toàn từ JsonElement
            fun JsonElement?.safeInt(): Int? = if (this == null || isJsonNull) null else asInt
            fun JsonElement?.safeString(): String? = if (this == null || isJsonNull) null else asString

            val id = obj.get("id").safeInt() ?: 0
            val userId = obj.get("userId").safeInt() ?: 0
            val borrowDate = obj.get("borrowDate").safeString()
            val dueDate = obj.get("dueDate").safeString()
            val getBookDate = obj.get("getBookDate").safeString()
            val status = obj.get("status").safeString()
            val soNgayTre = obj.get("soNgayTre").safeInt()

            val bookIds: List<BorrowedBookBrief>? = try {
                val bookIdsElement = obj.get("bookIds")
                if (bookIdsElement != null && !bookIdsElement.isJsonNull && bookIdsElement.isJsonArray) {
                    val bookIdsArray = bookIdsElement.asJsonArray
                    if (bookIdsArray.size() > 0) {
                        context.deserialize<List<BorrowedBookBrief>>(bookIdsArray, object : TypeToken<List<BorrowedBookBrief>>() {}.type)
                    } else null
                } else null
            } catch (e: Exception) {
                Log.w("RetrofitClient", "Không thể parse bookIds, trả về null. Lỗi: ${e.message}")
                null
            }

            return BorrowCardResponse(id, userId, borrowDate, dueDate, getBookDate, status, soNgayTre, bookIds)
        }
    }

    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .registerTypeAdapter(BorrowCardResponse::class.java, BorrowCardResponseDeserializer())
            .create()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}