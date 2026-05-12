package com.example.smartlibrary.data.model

data class Book(
    val id: String,
    val title: String,
    val author: String?,
    val publisher: String?,
    val year: Int?,
    val imageSrc: String,
    val available: Boolean,
    val borrowCount: Int = 0
)
