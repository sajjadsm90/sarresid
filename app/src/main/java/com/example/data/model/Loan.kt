package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,                  // عنوان وام / تسهیلات
    val bankName: String,               // نام بانک یا صندوق
    val totalAmount: Long,              // مبلغ کل وام
    val totalInstallments: Int,         // تعداد کل اقساط
    val installmentAmount: Long,        // مبلغ هر قسط
    val startDate: String,              // تاریخ اولین قسط (خورشیدی)
    val dayOfMonth: Int,                // روز ماه برای سررسید
    val interestRate: Double = 0.0,     // درصد سود سالیانه
    val description: String = "",
    val reminderDaysBefore: Int = 3,
    val createdAt: Long = System.currentTimeMillis()
)

enum class InstallmentStatus {
    PENDING,    // در انتظار پرداخت
    PAID,       // پرداخت شده
    OVERDUE     // معوقه / سررسید گذشته
}

@Entity(
    tableName = "installments",
    foreignKeys = [
        ForeignKey(
            entity = Loan::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["loanId"])]
)
data class Installment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanId: Long,
    val installmentNumber: Int,         // شماره قسط (۱ تا n)
    val amount: Long,                   // مبلغ قسط
    val dueDate: String,                // تاریخ سررسید (خورشیدی)
    val status: InstallmentStatus = InstallmentStatus.PENDING,
    val paidDate: String? = null,       // تاریخ پرداخت
    val note: String = ""
)
