package com.example.smartlibrary.data.model

data class News(
    val title: String,
    val date: String,
    val imageUrl: String,
    val content: String = "Nội dung đang cập nhật..."
)
