package com.example.smartlibrary.network

import android.content.Context
import com.example.smartlibrary.data.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/"
    private var sessionManager: SessionManager? = null

    fun initialize(context: Context) {
        sessionManager = SessionManager(context)
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
            val token = sessionManager?.getAccessToken()

            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            
            // QUAN TRỌNG: Không set Content-Type: application/json ở đây
            // Để Retrofit tự quyết định dựa trên annotation @Body hoặc @Multipart

            val newRequest = requestBuilder.build()
            chain.proceed(newRequest)
        }

        return builder.build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
