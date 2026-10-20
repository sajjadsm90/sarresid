package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ChequeType {
    PAYABLE,    // پرداختی (صادره توسط من)
    RECEIVABLE  // دریافتی (به نام من)
}

enum class ChequeStatus {
    PENDING,    // در انتظار وصول/پاس شدن
    PASSED,     // پاس شده / وصول شده
    BOUNCED,    // برگشت خورده
    CANCELLED   // باطل شده
}

@Entity(tableName = "cheques")
data class Cheque(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sayadNumber: String = "",       // شناسه ۱۶ رقمی صیادی
    val chequeNumber: String = "",      // شماره سریال چک
    val bankName: String,               // نام بانک صادرکننده
    val branch: String = "",            // شعبه
    val amount: Long,                   // مبلغ به ریال/تومان
    val dueDate: String,                // تاریخ سررسید خورشیدی (YYYY/MM/DD)
    val issueDate: String = "",         // تاریخ صدور خورشیدی
    val type: ChequeType,               // پرداختی یا دریافتی
    val partyName: String,              // نام گیرنده یا صادرکننده
    val status: ChequeStatus = ChequeStatus.PENDING,
    val description: String = "",
    val reminderDaysBefore: Int = 3,    // چند روز قبل یادآوری شود
    val createdAt: Long = System.currentTimeMillis()
)
