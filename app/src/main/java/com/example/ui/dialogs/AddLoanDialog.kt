package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.IranianBankList
import com.example.data.model.Loan
import com.example.ui.components.BankDropdown
import com.example.util.JalaliCalendar

@Composable
fun AddLoanDialog(
    onDismiss: () -> Unit,
    onConfirm: (Loan) -> Unit
) {
    val today = remember { JalaliCalendar.now() }

    var title by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("بانک ملی ایران") }
    var totalAmountText by remember { mutableStateOf("") }
    var totalInstallmentsText by remember { mutableStateOf("12") }
    var installmentAmountText by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(today.toString()) }
    var dayOfMonthText by remember { mutableStateOf(today.day.toString()) }
    var interestRateText by remember { mutableStateOf("18") }
    var description by remember { mutableStateOf("") }

    var isError by remember { mutableStateOf(false) }

    // Auto calculate installment amount if total amount and count change
    LaunchedEffect(totalAmountText, totalInstallmentsText) {
        val total = totalAmountText.toLongOrNull() ?: 0L
        val count = totalInstallmentsText.toIntOrNull() ?: 0
        if (total > 0 && count > 0 && installmentAmountText.isBlank()) {
            installmentAmountText = (total / count).toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ثبت وام و تسهیلات جدید",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان وام (مثال: وام مسکن، قرض‌الحسنه)") },
                    isError = isError && title.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                BankDropdown(
                    selectedBank = bankName,
                    onBankSelected = { bankName = it },
                    bankList = IranianBankList.getBankNames(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) totalAmountText = it },
                    label = { Text("مبلغ کل وام (تومان)") },
                    isError = isError && (totalAmountText.toLongOrNull() ?: 0L) <= 0,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = {
                        val amt = totalAmountText.toLongOrNull() ?: 0L
                        if (amt > 0) {
                            Text("${JalaliCalendar.formatPrice(amt)} تومان")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = totalInstallmentsText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) totalInstallmentsText = it },
                        label = { Text("تعداد اقساط") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = installmentAmountText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) installmentAmountText = it },
                        label = { Text("مبلغ هر قسط (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("تاریخ اولین قسط") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                    OutlinedTextField(
                        value = dayOfMonthText,
                        onValueChange = { if (it.all { c -> c.isDigit() } && (it.toIntOrNull() ?: 0) in 1..31) dayOfMonthText = it },
                        label = { Text("روز سررسید") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = interestRateText,
                    onValueChange = { interestRateText = it },
                    label = { Text("درصد سود سالیانه (اختیاری)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("شماره پرونده / توضیحات (اختیاری)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val total = totalAmountText.toLongOrNull() ?: 0L
                    val count = totalInstallmentsText.toIntOrNull() ?: 0
                    val instAmt = installmentAmountText.toLongOrNull() ?: (if (count > 0) total / count else 0L)
                    val day = dayOfMonthText.toIntOrNull() ?: 1

                    if (title.isNotBlank() && total > 0 && count > 0 && instAmt > 0) {
                        val loan = Loan(
                            title = title,
                            bankName = bankName,
                            totalAmount = total,
                            totalInstallments = count,
                            installmentAmount = instAmt,
                            startDate = startDate,
                            dayOfMonth = day,
                            interestRate = interestRateText.toDoubleOrNull() ?: 0.0,
                            description = description
                        )
                        onConfirm(loan)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("ثبت و ساخت اقساط")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
