package com.example.smartlibrary.data

import android.content.Context
import android.content.SharedPreferences

open class SessionManager(context: Context, fileName: String = "user_session") {
    private val prefs: SharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)

    companion object {
        const val USER_ID = "user_id"
        const val ACCESS_TOKEN = "access_token"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val READ_NOTIFICATIONS_PREFIX = "read_notifications_"
    }

    fun saveSession(userId: String, token: String, name: String?, email: String?) {
        prefs.edit().apply {
            putString(USER_ID, userId)
            putString(ACCESS_TOKEN, token)
            putString(USER_NAME, name)
            putString(USER_EMAIL, email)
        }.apply()
    }

    fun getUserId(): String? = prefs.getString(USER_ID, null)
    fun getAccessToken(): String? = prefs.getString(ACCESS_TOKEN, null)
    fun getUserName(): String? = prefs.getString(USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(USER_EMAIL, null)
    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun clearSession() {
        prefs.edit().apply {
            remove(USER_ID)
            remove(ACCESS_TOKEN)
            remove(USER_NAME)
            remove(USER_EMAIL)
        }.apply()
    }

    private fun getReadNotificationsKey(): String {
        val userId = getUserId() ?: return "read_notifications_anonymous"
        return READ_NOTIFICATIONS_PREFIX + userId
    }

    fun addReadNotification(id: String) {
        val key = getReadNotificationsKey()
        val currentSet = prefs.getStringSet(key, emptySet()) ?: emptySet()
        val newSet = currentSet.toMutableSet()
        newSet.add(id)
        prefs.edit().putStringSet(key, newSet).apply()
    }

    fun getReadNotifications(): Set<String> {
        val key = getReadNotificationsKey()
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    fun clearReadNotifications() {
        val key = getReadNotificationsKey()
        prefs.edit().remove(key).apply()
    }

    fun saveUserRole(role: String) {
        prefs.edit().putString("user_role", role).apply()
    }

    fun getUserRole(): String? = prefs.getString("user_role", null)
}