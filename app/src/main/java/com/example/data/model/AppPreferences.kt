package com.example.data.model

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sarresid_prefs", Context.MODE_PRIVATE)

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var reminderHour: Int
        get() = prefs.getInt(KEY_REMINDER_HOUR, 9)
        set(value) = prefs.edit().putInt(KEY_REMINDER_HOUR, value).apply()

    var reminderMinute: Int
        get() = prefs.getInt(KEY_REMINDER_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_REMINDER_MINUTE, value).apply()

    var defaultChequeDaysBefore: Int
        get() = prefs.getInt(KEY_DEFAULT_CHEQUE_DAYS, 3)
        set(value) = prefs.edit().putInt(KEY_DEFAULT_CHEQUE_DAYS, value).apply()

    var defaultLoanDaysBefore: Int
        get() = prefs.getInt(KEY_DEFAULT_LOAN_DAYS, 3)
        set(value) = prefs.edit().putInt(KEY_DEFAULT_LOAN_DAYS, value).apply()

    var isNotificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    var isPremiumUser: Boolean
        get() = prefs.getBoolean(KEY_IS_PREMIUM, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_PREMIUM, value).apply()

    var premiumPurchaseToken: String?
        get() = prefs.getString(KEY_PREMIUM_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_PREMIUM_TOKEN, value).apply()

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_REMINDER_HOUR = "key_reminder_hour"
        private const val KEY_REMINDER_MINUTE = "key_reminder_minute"
        private const val KEY_DEFAULT_CHEQUE_DAYS = "key_default_cheque_days"
        private const val KEY_DEFAULT_LOAN_DAYS = "key_default_loan_days"
        private const val KEY_NOTIFICATIONS_ENABLED = "key_notifications_enabled"
        private const val KEY_IS_PREMIUM = "key_is_premium"
        private const val KEY_PREMIUM_TOKEN = "key_premium_token"
    }
}
