package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.DashboardSummary
import com.example.util.JalaliCalendar

@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    cheques: List<Cheque>,
    installments: List<Installment>,
    loans: List<Loan>,
    isPremium: Boolean = false,
    onAddChequeClick: () -> Unit,
    onAddLoanClick: () -> Unit,
    onNavigateToCheques: () -> Unit,
    onNavigateToInstallments: () -> Unit,
    onUpgradeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val today = remember { JalaliCalendar.now() }
    val todayStr = remember { today.toString() }

    val upcomingCheques = remember(cheques) {
        cheques.filter {
            it.status == ChequeStatus.PENDING &&
                    JalaliCalendar.daysBetween(todayStr, it.dueDate) in 0..14
        }.sortedBy { it.dueDate }
    }

    val upcomingInstallments = remember(installments) {
        installments.filter {
            it.status == InstallmentStatus.PENDING &&
                    JalaliCalendar.daysBetween(todayStr, it.dueDate) in 0..14
        }.sortedBy { it.dueDate }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // VIP Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpgradeClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremium) Color(0xFF004D40) else Color(0xFFFFF8E1)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isPremium) Color(0xFFFFD700) else Color(0xFFFFB300)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isPremium) Color(0xFFFFD700) else Color(0xFFFFB300)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = if (isPremium) Color(0xFF3E2723) else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isPremium) "نسخه طلایی سررسید (نامحدود)" else "نسخه پایه (محدودیت ۱ چک و ۱ وام)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPremium) Color.White else Color(0xFF5D4037)
                            )
                            Text(
                                text = if (isPremium) "دسترسی کامل به امکانات پیشرفته و ثبت نامحدود" else "برای ثبت نامحدود چک و اقساط کلیک کنید",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isPremium) Color(0xFFFFE082) else Color(0xFF8D6E63),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isPremium) Color(0xFFFFD700).copy(alpha = 0.25f) else Color(0xFF00796B)
                    ) {
                        Text(
                            text = if (isPremium) "فعال ✓" else "ارتقا به طلایی",
                            color = if (isPremium) Color(0xFFFFD700) else Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Hero Header Card with Jalali Date
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(TealDark, TealPrimary, TealLight)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "پیشخوان سررسید مالی",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = today.formatPersian(includeMonthName = true),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Text(
                            text = "مدیریت و پیگیری هوشمند تمامی چک‌ها و اقساط بانکی",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onAddChequeClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = TealDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ثبت چک جدید", fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = onAddLoanClick,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.25f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ثبت وام جدید", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Key Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "چک‌های پرداختی من",
                        value = "${JalaliCalendar.formatPrice(summary.totalPayableCheques)} ت",
                        subtitle = "مجموع چک‌های صادره پاس‌نشده",
                        icon = Icons.Default.ArrowUpward,
                        iconBgColor = CrimsonLight,
                        iconColor = CrimsonRed,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "چک‌های دریافتی من",
                        value = "${JalaliCalendar.formatPrice(summary.totalReceivableCheques)} ت",
                        subtitle = "مجموع مطالبات در دست وصول",
                        icon = Icons.Default.ArrowDownward,
                        iconBgColor = EmeraldLight,
                        iconColor = EmeraldGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "اقساط فعال بانکی",
                        value = "${JalaliCalendar.toPersianDigits(summary.activeLoansCount)} پرونده",
                        subtitle = "بدهی کل: ${JalaliCalendar.formatPrice(summary.totalRemainingLoanDebt)} ت",
                        icon = Icons.Default.AccountBalance,
                        iconBgColor = AmberLight,
                        iconColor = AmberWarning,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "سررسیدهای نزدیک",
                        value = "${JalaliCalendar.toPersianDigits(summary.upcomingChequesCount + summary.upcomingInstallmentsCount)} فقره",
                        subtitle = "در ۷ روز آینده",
                        icon = Icons.Default.NotificationsActive,
                        iconBgColor = TealContainer,
                        iconColor = TealPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Upcoming Cheques Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سررسید چک‌های پیش‌رو (۱۴ روز آینده)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToCheques) {
                    Text("مشاهده همه (${JalaliCalendar.toPersianDigits(cheques.size)})")
                }
            }
        }

        if (upcomingCheques.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هیچ چکی در ۱۴ روز آینده سررسید ندارد 🎉",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(upcomingCheques) { cheque ->
                val daysLeft = JalaliCalendar.daysBetween(todayStr, cheque.dueDate)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCheques() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (cheque.type == ChequeType.PAYABLE) CrimsonLight else EmeraldLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (cheque.type == ChequeType.PAYABLE) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (cheque.type == ChequeType.PAYABLE) CrimsonRed else EmeraldGreen
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = cheque.partyName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${cheque.bankName} • سررسید: ${JalaliCalendar.toPersianDigits(cheque.dueDate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${JalaliCalendar.formatPrice(cheque.amount)} ت",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (cheque.type == ChequeType.PAYABLE) CrimsonRed else EmeraldGreen
                            )
                            StatusBadge(
                                text = if (daysLeft == 0L) "امروز" else "$daysLeft روز مانده",
                                backgroundColor = if (daysLeft <= 1) CrimsonLight else AmberLight,
                                textColor = if (daysLeft <= 1) CrimsonRed else AmberWarning
                            )
                        }
                    }
                }
            }
        }

        // Upcoming Installments Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سررسید اقساط پیش‌رو (۱۴ روز آینده)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToInstallments) {
                    Text("مشاهده اقساط")
                }
            }
        }

        if (upcomingInstallments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "قسطی برای ۱۴ روز آینده نزدیک سررسید نیست ✅",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(upcomingInstallments) { inst ->
                val loan = loans.find { it.id == inst.loanId }
                val daysLeft = JalaliCalendar.daysBetween(todayStr, inst.dueDate)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToInstallments() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(TealContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = TealPrimary
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = loan?.title ?: "قسط وام",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "قسط ${JalaliCalendar.toPersianDigits(inst.installmentNumber)} از ${JalaliCalendar.toPersianDigits(loan?.totalInstallments ?: 0)} • ${JalaliCalendar.toPersianDigits(inst.dueDate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${JalaliCalendar.formatPrice(inst.amount)} ت",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(
                                text = if (daysLeft == 0L) "امروز" else "$daysLeft روز مانده",
                                backgroundColor = if (daysLeft <= 1) CrimsonLight else AmberLight,
                                textColor = if (daysLeft <= 1) CrimsonRed else AmberWarning
                            )
                        }
                    }
                }
            }
        }
    }
}
