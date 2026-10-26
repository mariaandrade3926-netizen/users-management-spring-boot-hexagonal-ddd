package com.jcaa.usersmanagement.mobile.data

import android.content.Context
import android.util.Base64
import org.json.JSONObject

class SessionStore(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun baseUrl(): String = preferences.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL

    fun token(): String? = preferences.getString(KEY_TOKEN, null)

    fun email(): String? = preferences.getString(KEY_EMAIL, null)

    fun role(): String = preferences.getString(KEY_ROLE, "MEMBER") ?: "MEMBER"

    fun isLoggedIn(): Boolean = !token().isNullOrBlank()

    fun isAdmin(): Boolean = role().equals("ADMIN", ignoreCase = true)

    fun saveBaseUrl(baseUrl: String) {
        preferences.edit().putString(KEY_BASE_URL, baseUrl.trim()).apply()
    }

    fun saveSession(baseUrl: String, token: String, email: String, role: String) {
        preferences.edit()
            .putString(KEY_BASE_URL, baseUrl.trim())
            .putString(KEY_TOKEN, token)
            .putString(KEY_EMAIL, email)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun clearSession() {
        preferences.edit().remove(KEY_TOKEN).remove(KEY_EMAIL).remove(KEY_ROLE).apply()
    }

    companion object {
        private const val FILE_NAME = "users_session"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_TOKEN = "token"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8080"
    }
}

object JwtPayload {
    fun role(token: String): String {
        return try {
            val payload = token.split(".").getOrNull(1) ?: return "MEMBER"
            val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
            val json = String(Base64.decode(padded, Base64.URL_SAFE or Base64.NO_WRAP), Charsets.UTF_8)
            JSONObject(json).optString("role", "MEMBER").ifBlank { "MEMBER" }
        } catch (_: Exception) {
            "MEMBER"
        }
    }
}
