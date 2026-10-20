package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.model.ChequeStatus
import com.example.data.model.InstallmentStatus
import com.example.notification.NotificationHelper
import com.example.util.JalaliCalendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val db = AppDatabase.getDatabase(context)
        val todayStr = JalaliCalendar.now().toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cheques = db.chequeDao().getAllCheques().firstOrNull() ?: emptyList()
                val pendingCheques = cheques.filter { it.status == ChequeStatus.PENDING }

                for (cheque in pendingCheques) {
                    val daysLeft = JalaliCalendar.daysBetween(todayStr, cheque.dueDate)
                    if (daysLeft in 0..cheque.reminderDaysBefore) {
                        val daysText = if (daysLeft == 0L) "امروز" else "تا $daysLeft روز دیگر"
                        val formattedAmount = JalaliCalendar.formatPrice(cheque.amount)
                        NotificationHelper.showNotification(
                            context,
                            cheque.id.toInt(),
                            "سررسید چک: ${cheque.partyName}",
                            "چک $daysText به مبلغ $formattedAmount تومان سررسید می‌شود."
                        )
                    }
                }

                val installments = db.installmentDao().getAllInstallments().firstOrNull() ?: emptyList()
                val pendingInstallments = installments.filter { it.status == InstallmentStatus.PENDING }

                for (inst in pendingInstallments) {
                    val daysLeft = JalaliCalendar.daysBetween(todayStr, inst.dueDate)
                    if (daysLeft in 0..3) {
                        val daysText = if (daysLeft == 0L) "امروز" else "تا $daysLeft روز دیگر"
                        val formattedAmount = JalaliCalendar.formatPrice(inst.amount)
                        NotificationHelper.showNotification(
                            context,
                            (inst.id + 10000).toInt(),
                            "سررسید قسط شماره ${inst.installmentNumber}",
                            "قسط $daysText به مبلغ $formattedAmount تومان سررسید می‌شود."
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
