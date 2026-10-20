package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.JalaliCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    cheques: List<Cheque>,
    installments: List<Installment>,
    loans: List<Loan>,
    onAddChequeWithDate: (String) -> Unit,
    onAddLoanClick: () -> Unit,
    onUpdateChequeStatus: (Long, ChequeStatus) -> Unit,
    onUpdateInstallmentStatus: (Long, InstallmentStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { JalaliCalendar.now() }
    val todayDateStr = remember { today.toString() }

    var currentYear by remember { mutableStateOf(today.year) }
    var currentMonth by remember { mutableStateOf(today.month) }
    var selectedDay by remember { mutableStateOf(today.day) }

    val daysInCurrentMonth = remember(currentYear, currentMonth) {
        JalaliCalendar.getDaysInMonth(currentYear, currentMonth)
    }

    val firstDayOffset = remember(currentYear, currentMonth) {
        JalaliCalendar.getFirstDayOfWeekInMonth(currentYear, currentMonth)
    }

    val selectedDateStr = remember(currentYear, currentMonth, selectedDay) {
        String.format("%04d/%02d/%02d", currentYear, currentMonth, selectedDay)
    }

    // Filter events for the entire current month
    val monthPrefix = remember(currentYear, currentMonth) {
        String.format("%04d/%02d", currentYear, currentMonth)
    }

    val chequesInMonth = remember(cheques, monthPrefix) {
        cheques.filter { it.dueDate.startsWith(monthPrefix) }
    }

    val installmentsInMonth = remember(installments, monthPrefix) {
        installments.filter { it.dueDate.startsWith(monthPrefix) }
    }

    // Monthly financial totals
    val monthPayableCheques = remember(chequesInMonth) {
        chequesInMonth.filter { it.type == ChequeType.PAYABLE && it.status == ChequeStatus.PENDING }.sumOf { it.amount }
    }
    val monthReceivableCheques = remember(chequesInMonth) {
        chequesInMonth.filter { it.type == ChequeType.RECEIVABLE && it.status == ChequeStatus.PENDING }.sumOf { it.amount }
    }
    val monthInstallmentsTotal = remember(installmentsInMonth) {
        installmentsInMonth.filter { it.status == InstallmentStatus.PENDING }.sumOf { it.amount }
    }

    // Filter events for the currently selected day
    val chequesOnSelectedDay = remember(cheques, selectedDateStr) {
        cheques.filter { it.dueDate == selectedDateStr }
    }

    val installmentsOnSelectedDay = remember(installments, selectedDateStr) {
        installments.filter { it.dueDate == selectedDateStr }
    }

    val weekdays = remember {
        listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp)
    ) {
        // Top Month Navigation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Month Selector Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Month Button (< in RTL means next chronological month)
                        IconButton(
                            onClick = {
                                if (currentMonth > 1) {
                                    currentMonth--
                                } else {
                                    currentYear--
                                    currentMonth = 12
                                }
                                selectedDay = 1.coerceAtMost(JalaliCalendar.getDaysInMonth(currentYear, currentMonth))
                            }
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "ماه قبل")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${JalaliCalendar.getPersianMonthName(currentMonth)} ${JalaliCalendar.toPersianDigits(currentYear)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (currentYear != today.year || currentMonth != today.month) {
                                Surface(
                                    onClick = {
                                        currentYear = today.year
                                        currentMonth = today.month
                                        selectedDay = today.day
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = TealContainer
                                ) {
                                    Text(
                                        text = "امروز",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TealPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Next Month Button
                        IconButton(
                            onClick = {
                                if (currentMonth < 12) {
                                    currentMonth++
                                } else {
                                    currentYear++
                                    currentMonth = 1
                                }
                                selectedDay = 1.coerceAtMost(JalaliCalendar.getDaysInMonth(currentYear, currentMonth))
                            }
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "ماه بعد")
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Weekday Headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekdays.forEachIndexed { index, dayName ->
                            val isFriday = index == 6
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFriday) CrimsonRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 7-Column Calendar Grid
                    val totalCells = firstDayOffset + daysInCurrentMonth
                    val rowsCount = (totalCells + 6) / 7

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until rowsCount) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayNumber = cellIndex - firstDayOffset + 1

                                    if (dayNumber in 1..daysInCurrentMonth) {
                                        val dayDateStr = String.format("%04d/%02d/%02d", currentYear, currentMonth, dayNumber)
                                        val isToday = dayDateStr == todayDateStr
                                        val isSelected = dayNumber == selectedDay

                                        // Find events on this day
                                        val dayCheques = cheques.filter { it.dueDate == dayDateStr }
                                        val dayInstallments = installments.filter { it.dueDate == dayDateStr }

                                        val hasPendingPayable = dayCheques.any { it.type == ChequeType.PAYABLE && it.status == ChequeStatus.PENDING }
                                        val hasPendingReceivable = dayCheques.any { it.type == ChequeType.RECEIVABLE && it.status == ChequeStatus.PENDING }
                                        val hasPendingInstallment = dayInstallments.any { it.status == InstallmentStatus.PENDING }
                                        val hasPassedEvents = (dayCheques.any { it.status == ChequeStatus.PASSED } || dayInstallments.any { it.status == InstallmentStatus.PAID }) &&
                                                !hasPendingPayable && !hasPendingReceivable && !hasPendingInstallment

                                        CalendarDayCell(
                                            dayNumber = dayNumber,
                                            isToday = isToday,
                                            isSelected = isSelected,
                                            isFriday = col == 6,
                                            hasPendingPayable = hasPendingPayable,
                                            hasPendingReceivable = hasPendingReceivable,
                                            hasPendingInstallment = hasPendingInstallment,
                                            hasPassedEvents = hasPassedEvents,
                                            onClick = { selectedDay = dayNumber },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Monthly Summary Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("چک‌های پرداختی ماه", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = "${JalaliCalendar.formatPrice(monthPayableCheques)} ت",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonRed
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .height(32.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("چک‌های دریافتی ماه", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = "${JalaliCalendar.formatPrice(monthReceivableCheques)} ت",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .height(32.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("اقساط این ماه", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = "${JalaliCalendar.formatPrice(monthInstallmentsTotal)} ت",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                }
            }
        }

        // Legend / Indicator Guide
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CrimsonRed))
                    Text("چک پرداختی", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldGreen))
                    Text("چک دریافتی", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AmberWarning))
                    Text("سررسید قسط", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Selected Day Events Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedSelectedDay = remember(currentYear, currentMonth, selectedDay) {
                    "${JalaliCalendar.toPersianDigits(selectedDay)} ${JalaliCalendar.getPersianMonthName(currentMonth)} ${JalaliCalendar.toPersianDigits(currentYear)}"
                }
                Text(
                    text = "سررسیدهای روز $formattedSelectedDay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onAddChequeWithDate(selectedDateStr) }) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "ثبت برای این روز", tint = TealPrimary)
                }
            }
        }

        // Events on selected day
        if (chequesOnSelectedDay.isEmpty() && installmentsOnSelectedDay.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "هیچ سررسیدی برای این تاریخ ثبت نشده است ✨",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { onAddChequeWithDate(selectedDateStr) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ثبت چک برای این روز")
                        }
                    }
                }
            }
        } else {
            // Show Cheques on selected day
            items(chequesOnSelectedDay, key = { "cheque_${it.id}" }) { cheque ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                    .background(if (cheque.type == ChequeType.PAYABLE) CrimsonLight else EmeraldLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (cheque.type == ChequeType.PAYABLE) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (cheque.type == ChequeType.PAYABLE) CrimsonRed else EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = cheque.partyName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${cheque.bankName} • ${if (cheque.type == ChequeType.PAYABLE) "پرداختی" else "دریافتی"}",
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
                                text = when (cheque.status) {
                                    ChequeStatus.PASSED -> "وصول شد"
                                    ChequeStatus.BOUNCED -> "برگشتی"
                                    ChequeStatus.CANCELLED -> "باطل"
                                    ChequeStatus.PENDING -> "در انتظار"
                                },
                                backgroundColor = when (cheque.status) {
                                    ChequeStatus.PASSED -> EmeraldLight
                                    ChequeStatus.BOUNCED -> CrimsonLight
                                    ChequeStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
                                    ChequeStatus.PENDING -> AmberLight
                                },
                                textColor = when (cheque.status) {
                                    ChequeStatus.PASSED -> EmeraldGreen
                                    ChequeStatus.BOUNCED -> CrimsonRed
                                    ChequeStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                                    ChequeStatus.PENDING -> AmberWarning
                                }
                            )
                        }
                    }
                }
            }

            // Show Installments on selected day
            items(installmentsOnSelectedDay, key = { "inst_${it.id}" }) { inst ->
                val loan = loans.find { it.id == inst.loanId }
                val isPaid = inst.status == InstallmentStatus.PAID
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            Checkbox(
                                checked = isPaid,
                                onCheckedChange = {
                                    val nextStatus = if (isPaid) InstallmentStatus.PENDING else InstallmentStatus.PAID
                                    onUpdateInstallmentStatus(inst.id, nextStatus)
                                }
                            )
                            Column {
                                Text(
                                    text = "${loan?.title ?: "وام"} (قسط ${JalaliCalendar.toPersianDigits(inst.installmentNumber)})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = loan?.bankName ?: "بانک",
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
                                color = if (isPaid) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isPaid) "پرداخت شده ✓" else "در انتظار پرداخت",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPaid) EmeraldGreen else AmberWarning,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    dayNumber: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isFriday: Boolean,
    hasPendingPayable: Boolean,
    hasPendingReceivable: Boolean,
    hasPendingInstallment: Boolean,
    hasPassedEvents: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                } else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = JalaliCalendar.toPersianDigits(dayNumber),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    isFriday -> CrimsonRed
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // Event dots below day number
            Row(
                modifier = Modifier
                    .height(6.dp)
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasPendingPayable) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else CrimsonRed)
                    )
                }
                if (hasPendingReceivable) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else EmeraldGreen)
                    )
                }
                if (hasPendingInstallment) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else AmberWarning)
                    )
                }
                if (hasPassedEvents) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else MaterialTheme.colorScheme.outline)
                    )
                }
            }
        }
    }
}
