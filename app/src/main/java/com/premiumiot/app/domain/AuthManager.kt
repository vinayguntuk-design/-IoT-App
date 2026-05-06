package com.premiumiot.app.domain

import android.content.Context

class AuthManager(context: Context) {
    private val prefs = context.getSharedPreferences("premium_iot_auth", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun login(username: String, password: String): Boolean {
        val accepted = username == DEFAULT_USERNAME && password == DEFAULT_PASSWORD
        if (accepted) {
            prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
        }
        return accepted
    }

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    companion object {
        const val DEFAULT_USERNAME = "admin"
        const val DEFAULT_PASSWORD = "admin123"
        private const val KEY_LOGGED_IN = "logged_in"
    }
}
