package com.example.smartlibrary.util

import java.text.SimpleDateFormat
import java.util.Locale

fun formatDate(dateString: String?, defaultValue: String = "N/A"): String {
    if (dateString.isNullOrBlank()) return defaultValue
    return try {
        // Try parsing as yyyy-MM-dd
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = parser.parse(dateString)
        if (date != null) formatter.format(date) else defaultValue
    } catch (e: Exception) {
        // If it's already in a different format or fails, return as is
        dateString ?: defaultValue
    }
}
