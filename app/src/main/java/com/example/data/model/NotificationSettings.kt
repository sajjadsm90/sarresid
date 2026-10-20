package com.example.data.model

data class NotificationSettings(
    val isEnabled: Boolean = true,
    val reminderHour: Int = 9,          // ساعت یادآوری (مثلا ۹ صبح)
    val reminderMinute: Int = 0,
    val defaultChequeDaysBefore: Int = 3,
    val defaultLoanDaysBefore: Int = 3,
    val vibrateEnabled: Boolean = true,
    val soundEnabled: Boolean = true
)
