package com.leadrescue.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Small app settings kept in SharedPreferences. */
class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _businessName = MutableStateFlow(prefs.getString(KEY_BUSINESS_NAME, "") ?: "")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    fun setBusinessName(value: String) {
        prefs.edit().putString(KEY_BUSINESS_NAME, value).apply()
        _businessName.value = value
    }

    var askedForNotifications: Boolean
        get() = prefs.getBoolean(KEY_ASKED_NOTIFICATIONS, false)
        set(value) = prefs.edit().putBoolean(KEY_ASKED_NOTIFICATIONS, value).apply()

    private companion object {
        const val KEY_BUSINESS_NAME = "business_name"
        const val KEY_ASKED_NOTIFICATIONS = "asked_notifications"
    }
}
