package com.example.smartlibrary.data.model

data class UserInfo(
    val fullName: String,
    val email: String,
    val phone: String,
    val birthdate: String?,
    val joinDate: String?,
    val avatarUrl: String?,
    val studentId: String?
)
