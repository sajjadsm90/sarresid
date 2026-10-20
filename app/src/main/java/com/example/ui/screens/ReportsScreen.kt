package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.util.JalaliCalendar

@Composable
fun ReportsScreen(
    cheques: List<Cheque>,
    loans: List<Loan>,
    installments: List<Installment>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val today = remember { JalaliCalendar.now() }

    val passedCheques = cheques.filter { it.status == ChequeStatus.PASSED }
    val pendingCheques = cheques.filter { it.status == ChequeStatus.PENDING }
    val bouncedCheques = cheques.filter { it.status == ChequeStatus.BOUNCED }

    val totalPayable = pendingCheques.filter { it.type == ChequeType.PAYABLE }.sumOf { it.amount }
    val totalReceivable = pendingCheques.filter { it.type == ChequeType.RECEIVABLE }.sumOf { it.amount }

    val paidInstallments = installments.filter { it.status == InstallmentStatus.PAID }
    val pendingInstallments = installments.filter { it.status == InstallmentStatus.PENDING }
    val remainingDebt = pendingInstallments.sumOf { it.amount }
    val totalPaidDebt = paidInstallments.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "گزارش و تراز مالی جامع",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = {
                            shareFinancialReport(
                                context = context,
                                payable = totalPayable,
                                receivable = totalReceivable,
                                remainingDebt = remainingDebt,
                                totalCheques = cheques.size,
                                totalLoans = loans.size
                            )
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "اشتراک گزارش", tint = TealDark)
                        }
                    }

                    Text(
                        text = "تاریخ گزارش: ${today.formatPersian(includeMonthName = true)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            Text(
                text = "خلاصه چک‌ها",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "وصول شده / پاس شده",
                    value = "${JalaliCalendar.toPersianDigits(passedCheques.size)} فقره",
                    subtitle = "مبلغ: ${JalaliCalendar.formatPrice(passedCheques.sumOf { it.amount })} ت",
                    icon = Icons.Default.CheckCircle,
                    iconBgColor = EmeraldLight,
                    iconColor = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "برگشت خورده",
                    value = "${JalaliCalendar.toPersianDigits(bouncedCheques.size)} فقره",
                    subtitle = "مبلغ: ${JalaliCalendar.formatPrice(bouncedCheques.sumOf { it.amount })} ت",
                    icon = Icons.Default.Cancel,
                    iconBgColor = CrimsonLight,
                    iconColor = CrimsonRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "خلاصه تسهیلات و وام‌ها",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "اقساط پرداخت شده",
                    value = "${JalaliCalendar.toPersianDigits(paidInstallments.size)} قسط",
                    subtitle = "${JalaliCalendar.formatPrice(totalPaidDebt)} ت",
                    icon = Icons.Default.DoneAll,
                    iconBgColor = TealContainer,
                    iconColor = TealPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "مانده کل اقساط",
                    value = "${JalaliCalendar.toPersianDigits(pendingInstallments.size)} قسط",
                    subtitle = "${JalaliCalendar.formatPrice(remainingDebt)} ت",
                    icon = Icons.Default.HourglassEmpty,
                    iconBgColor = AmberLight,
                    iconColor = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun shareFinancialReport(
    context: Context,
    payable: Long,
    receivable: Long,
    remainingDebt: Long,
    totalCheques: Int,
    totalLoans: Int
) {
    val jalaliNow = JalaliCalendar.now().formatPersian(includeMonthName = true)
    val text = buildString {
        appendLine("📊 *گزارش خلاصه وضعیت مالی - برنامه سررسید*")
        appendLine("📅 تاریخ: $jalaliNow")
        appendLine("───────────────")
        appendLine("🔹 *تعداد چک‌ها:* ${JalaliCalendar.toPersianDigits(totalCheques)} فقره")
        appendLine("🔹 *مجموع چک‌های پرداختی:* ${JalaliCalendar.formatPrice(payable)} تومان")
        appendLine("🔹 *مجموع چک‌های دریافتی:* ${JalaliCalendar.formatPrice(receivable)} تومان")
        appendLine("🔹 *تعداد وام‌ها:* ${JalaliCalendar.toPersianDigits(totalLoans)} پرونده")
        appendLine("🔹 *مانده بدهی اقساط:* ${JalaliCalendar.formatPrice(remainingDebt)} تومان")
        appendLine("───────────────")
        appendLine("ثبت و مدیریت شده با اپلیکیشن سررسید 📱")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "ارسال گزارش مالی"))
}
