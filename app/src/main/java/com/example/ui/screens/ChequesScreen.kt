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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Cheque
import com.example.data.model.ChequeStatus
import com.example.data.model.ChequeType
import com.example.ui.components.StatusBadge
import com.example.ui.dialogs.AddChequeDialog
import com.example.ui.theme.*
import com.example.util.JalaliCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChequesScreen(
    cheques: List<Cheque>,
    isPremium: Boolean,
    onRequestAddCheque: () -> Unit,
    onAddCheque: (Cheque) -> Unit,
    onUpdateCheque: (Cheque) -> Unit,
    onDeleteCheque: (Cheque) -> Unit,
    onUpdateStatus: (Long, ChequeStatus) -> Unit,
    onUpgradeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilterType by remember { mutableStateOf<ChequeType?>(null) }
    var selectedFilterStatus by remember { mutableStateOf<ChequeStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var chequeToEdit by remember { mutableStateOf<Cheque?>(null) }
    var chequeForStatusMenu by remember { mutableStateOf<Cheque?>(null) }

    val todayStr = remember { JalaliCalendar.now().toString() }

    val filteredCheques = remember(cheques, selectedFilterType, selectedFilterStatus, searchQuery) {
        cheques.filter { cheque ->
            (selectedFilterType == null || cheque.type == selectedFilterType) &&
                    (selectedFilterStatus == null || cheque.status == selectedFilterStatus) &&
                    (searchQuery.isBlank() ||
                            cheque.partyName.contains(searchQuery, ignoreCase = true) ||
                            cheque.bankName.contains(searchQuery, ignoreCase = true) ||
                            cheque.sayadNumber.contains(searchQuery) ||
                            cheque.chequeNumber.contains(searchQuery))
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRequestAddCheque,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "ثبت چک جدید")
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
            if (!isPremium && cheques.isNotEmpty()) {
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
                                text = "نسخه رایگان: ${cheques.size} از ۱ چک ثبت شده",
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

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("جستجو در چک‌ها (نام طرف حساب، بانک، صیادی)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (!isPremium && cheques.isNotEmpty()) 0.dp else 8.dp)
            )

            // Filter Chips (Type)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilterType == null,
                    onClick = { selectedFilterType = null },
                    label = { Text("همه چک‌ها (${cheques.size})") }
                )
                FilterChip(
                    selected = selectedFilterType == ChequeType.PAYABLE,
                    onClick = { selectedFilterType = if (selectedFilterType == ChequeType.PAYABLE) null else ChequeType.PAYABLE },
                    label = { Text("پرداختی (${cheques.count { it.type == ChequeType.PAYABLE }})") }
                )
                FilterChip(
                    selected = selectedFilterType == ChequeType.RECEIVABLE,
                    onClick = { selectedFilterType = if (selectedFilterType == ChequeType.RECEIVABLE) null else ChequeType.RECEIVABLE },
                    label = { Text("دریافتی (${cheques.count { it.type == ChequeType.RECEIVABLE }})") }
                )
            }

            // Cheque List
            if (filteredCheques.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedFilterType != null) "چکی با این فیلتر یافت نشد" else "هنوز چکی ثبت نکرده‌اید",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.isBlank() && selectedFilterType == null) {
                            Button(onClick = onRequestAddCheque) {
                                Text("ثبت اولین چک")
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
                    items(filteredCheques, key = { it.id }) { cheque ->
                        val daysLeft = JalaliCalendar.daysBetween(todayStr, cheque.dueDate)
                        ChequeCard(
                            cheque = cheque,
                            daysLeft = daysLeft,
                            onEdit = { chequeToEdit = cheque },
                            onDelete = { onDeleteCheque(cheque) },
                            onStatusClick = { chequeForStatusMenu = cheque }
                        )
                    }
                }
            }
        }
    }

    if (chequeToEdit != null) {
        AddChequeDialog(
            initialCheque = chequeToEdit,
            onDismiss = { chequeToEdit = null },
            onConfirm = {
                onUpdateCheque(it)
                chequeToEdit = null
            }
        )
    }

    if (chequeForStatusMenu != null) {
        val cheque = chequeForStatusMenu!!
        AlertDialog(
            onDismissRequest = { chequeForStatusMenu = null },
            title = { Text("تغییر وضعیت چک ${cheque.partyName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            onUpdateStatus(cheque.id, ChequeStatus.PASSED)
                            chequeForStatusMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✅ وصول / پاس شد (تکمیل شده)")
                    }
                    TextButton(
                        onClick = {
                            onUpdateStatus(cheque.id, ChequeStatus.PENDING)
                            chequeForStatusMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⏳ در انتظار وصول")
                    }
                    TextButton(
                        onClick = {
                            onUpdateStatus(cheque.id, ChequeStatus.BOUNCED)
                            chequeForStatusMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("❌ برگشت خورده")
                    }
                    TextButton(
                        onClick = {
                            onUpdateStatus(cheque.id, ChequeStatus.CANCELLED)
                            chequeForStatusMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🚫 باطل / لغو شده")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { chequeForStatusMenu = null }) {
                    Text("بستن")
                }
            }
        )
    }
}

@Composable
fun ChequeCard(
    cheque: Cheque,
    daysLeft: Long,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (cheque.type == ChequeType.PAYABLE) CrimsonLight else EmeraldLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (cheque.type == ChequeType.PAYABLE) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (cheque.type == ChequeType.PAYABLE) CrimsonRed else EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = cheque.partyName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (cheque.type == ChequeType.PAYABLE) "پرداختی به" else "دریافتی از",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status clickable badge
                Surface(
                    onClick = onStatusClick,
                    shape = RoundedCornerShape(12.dp),
                    color = when (cheque.status) {
                        ChequeStatus.PASSED -> EmeraldLight
                        ChequeStatus.BOUNCED -> CrimsonLight
                        ChequeStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
                        ChequeStatus.PENDING -> if (daysLeft <= 1) CrimsonLight else AmberLight
                    }
                ) {
                    Text(
                        text = when (cheque.status) {
                            ChequeStatus.PASSED -> "وصول شد ✓"
                            ChequeStatus.BOUNCED -> "برگشت خورده ✕"
                            ChequeStatus.CANCELLED -> "باطل شده"
                            ChequeStatus.PENDING -> if (daysLeft == 0L) "امروز" else if (daysLeft < 0) "سررسید گذشته" else "$daysLeft روز مانده"
                        },
                        color = when (cheque.status) {
                            ChequeStatus.PASSED -> EmeraldGreen
                            ChequeStatus.BOUNCED -> CrimsonRed
                            ChequeStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                            ChequeStatus.PENDING -> if (daysLeft <= 1) CrimsonRed else AmberWarning
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "مبلغ چک",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${JalaliCalendar.formatPrice(cheque.amount)} تومان",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (cheque.type == ChequeType.PAYABLE) CrimsonRed else EmeraldGreen
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "تاریخ سررسید",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = JalaliCalendar.toPersianDigits(cheque.dueDate),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Bank details and Sayad ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${cheque.bankName}${if (cheque.branch.isNotBlank()) " - شعبه ${cheque.branch}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (cheque.sayadNumber.isNotBlank()) {
                    Text(
                        text = "صیادی: ${JalaliCalendar.toPersianDigits(cheque.sayadNumber)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = CrimsonRed)
                }
            }
        }
    }
}
