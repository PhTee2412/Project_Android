package com.example.smartlibrary.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        const val USER_ID = "user_id"
        const val ACCESS_TOKEN = "access_token"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val READ_NOTIFICATIONS = "read_notifications"
    }

    fun saveSession(userId: String, token: String, name: String?, email: String?) {
        val editor = prefs.edit()
        editor.putString(USER_ID, userId)
        editor.putString(ACCESS_TOKEN, token)
        editor.putString(USER_NAME, name)
        editor.putString(USER_EMAIL, email)
        editor.apply()
    }

    fun getUserId(): String? = prefs.getString(USER_ID, null)
    fun getAccessToken(): String? = prefs.getString(ACCESS_TOKEN, null)
    fun getUserName(): String? = prefs.getString(USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(USER_EMAIL, null)
    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun addReadNotification(id: String) {
        val readSet = getReadNotifications().toMutableSet()
        readSet.add(id)
        prefs.edit().putStringSet(READ_NOTIFICATIONS, readSet).apply()
    }

    fun getReadNotifications(): Set<String> {
        return prefs.getStringSet(READ_NOTIFICATIONS, emptySet()) ?: emptySet()
    }

    fun clearReadNotifications() {
        prefs.edit().remove(READ_NOTIFICATIONS).apply()
    }
}