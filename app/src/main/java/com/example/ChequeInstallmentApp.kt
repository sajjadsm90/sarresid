package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.notification.NotificationHelper

class ChequeInstallmentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        AppDatabase.getDatabase(this)
    }
}
