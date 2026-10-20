package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Installment
import com.example.data.model.InstallmentStatus
import com.example.data.model.Loan
import com.example.ui.dialogs.AddLoanDialog
import com.example.ui.theme.*
import com.example.util.JalaliCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallmentsScreen(
    loans: List<Loan>,
    installments: List<Installment>,
    isPremium: Boolean,
    onRequestAddLoan: () -> Unit,
    onAddLoan: (Loan) -> Unit,
    onDeleteLoan: (Loan) -> Unit,
    onUpdateInstallmentStatus: (Long, InstallmentStatus) -> Unit,
    onUpgradeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Upcoming installments, 1: Loans list
    val todayStr = remember { JalaliCalendar.now().toString() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRequestAddLoan,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "ثبت وام جدید")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Free quota badge if not premium
            if (!isPremium && loans.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AmberWarning.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                            Text(
                                text = "نسخه رایگان: ${loans.size} از ۱ وام ثبت شده",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(onClick = onUpgradeClick) {
                            Text("ارتقا به طلایی", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(top = if (!isPremium && loans.isNotEmpty()) 0.dp else 8.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("لیست اقساط (${installments.count { it.status == InstallmentStatus.PENDING }})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("پرونده وام‌ها (${loans.size})") }
                )
            }

            if (selectedTab == 0) {
                // Installments List Tab
                val pendingInstallments = installments.sortedBy { it.dueDate }
                if (pendingInstallments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "هیچ قسطی در صف پرداخت نیست",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (loans.isEmpty()) {
                                Button(onClick = onRequestAddLoan) {
                                    Text("ثبت اولین وام")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(pendingInstallments, key = { it.id }) { inst ->
                            val loan = loans.find { it.id == inst.loanId }
                            val daysLeft = JalaliCalendar.daysBetween(todayStr, inst.dueDate)
                            InstallmentCard(
                                installment = inst,
                                loan = loan,
                                daysLeft = daysLeft,
                                onToggleStatus = {
                                    val nextStatus = if (inst.status == InstallmentStatus.PAID) InstallmentStatus.PENDING else InstallmentStatus.PAID
                                    onUpdateInstallmentStatus(inst.id, nextStatus)
                                }
                            )
                        }
                    }
                }
            } else {
                // Loans List Tab
                if (loans.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = onRequestAddLoan) {
                            Text("ثبت اولین وام")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(loans, key = { it.id }) { loan ->
                            val loanInsts = installments.filter { it.loanId == loan.id }
                            val paidCount = loanInsts.count { it.status == InstallmentStatus.PAID }
                            val remainingCount = loan.totalInstallments - paidCount
                            val remainingAmount = loanInsts.filter { it.status == InstallmentStatus.PENDING }.sumOf { it.amount }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                            text = loan.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(onClick = { onDeleteLoan(loan) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف وام", tint = CrimsonRed)
                                        }
                                    }

                                    Text(
                                        text = "${loan.bankName} • روز سررسید: ${JalaliCalendar.toPersianDigits(loan.dayOfMonth)} هر ماه",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Progress Bar
                                    val progress = if (loan.totalInstallments > 0) paidCount.toFloat() / loan.totalInstallments.toFloat() else 0f
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp),
                                            color = TealPrimary,
                                            trackColor = TealContainer
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "پرداخت شده: ${JalaliCalendar.toPersianDigits(paidCount)} از ${JalaliCalendar.toPersianDigits(loan.totalInstallments)} قسط",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${(progress * 100).toInt()}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TealPrimary
                                            )
                                        }
                                    }

                                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("مبلغ هر قسط", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${JalaliCalendar.formatPrice(loan.installmentAmount)} ت", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("مانده کل بدهی", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${JalaliCalendar.formatPrice(remainingAmount)} ت", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CrimsonRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstallmentCard(
    installment: Installment,
    loan: Loan?,
    daysLeft: Long,
    onToggleStatus: () -> Unit
) {
    val isPaid = installment.status == InstallmentStatus.PAID
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Checkbox(
                    checked = isPaid,
                    onCheckedChange = { onToggleStatus() }
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${loan?.title ?: "وام"} (قسط ${JalaliCalendar.toPersianDigits(installment.installmentNumber)})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${loan?.bankName ?: ""} • سررسید: ${JalaliCalendar.toPersianDigits(installment.dueDate)}",
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
                    text = "${JalaliCalendar.formatPrice(installment.amount)} ت",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaid) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                )
                if (isPaid) {
                    Text(
                        text = "پرداخت شده ✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (daysLeft <= 1) CrimsonLight else AmberLight
                    ) {
                        Text(
                            text = if (daysLeft == 0L) "امروز" else if (daysLeft < 0) "معوقه" else "$daysLeft روز مانده",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (daysLeft <= 1) CrimsonRed else AmberWarning,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
