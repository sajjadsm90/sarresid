package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Cheque
import com.example.data.model.ChequeStatus
import com.example.data.model.ChequeType
import com.example.data.model.IranianBankList
import com.example.ui.components.BankDropdown
import com.example.util.JalaliCalendar

@Composable
fun AddChequeDialog(
    initialCheque: Cheque? = null,
    initialDueDate: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (Cheque) -> Unit
) {
    val today = remember { JalaliCalendar.now().toString() }

    var type by remember { mutableStateOf(initialCheque?.type ?: ChequeType.PAYABLE) }
    var sayadNumber by remember { mutableStateOf(initialCheque?.sayadNumber ?: "") }
    var chequeNumber by remember { mutableStateOf(initialCheque?.chequeNumber ?: "") }
    var bankName by remember { mutableStateOf(initialCheque?.bankName ?: "بانک ملی ایران") }
    var branch by remember { mutableStateOf(initialCheque?.branch ?: "") }
    var amountText by remember { mutableStateOf(initialCheque?.amount?.toString() ?: "") }
    var dueDate by remember { mutableStateOf(initialCheque?.dueDate ?: initialDueDate ?: today) }
    var issueDate by remember { mutableStateOf(initialCheque?.issueDate ?: today) }
    var partyName by remember { mutableStateOf(initialCheque?.partyName ?: "") }
    var description by remember { mutableStateOf(initialCheque?.description ?: "") }
    var reminderDays by remember { mutableStateOf(initialCheque?.reminderDaysBefore?.toString() ?: "3") }

    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialCheque == null) "ثبت چک جدید" else "ویرایش چک",
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
                // Cheque Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == ChequeType.PAYABLE,
                        onClick = { type = ChequeType.PAYABLE },
                        label = { Text("چک پرداختی (صادره)") },
                        leadingIcon = {
                            if (type == ChequeType.PAYABLE) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == ChequeType.RECEIVABLE,
                        onClick = { type = ChequeType.RECEIVABLE },
                        label = { Text("چک دریافتی") },
                        leadingIcon = {
                            if (type == ChequeType.RECEIVABLE) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Party Name (In favor of / Drawer)
                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text(if (type == ChequeType.PAYABLE) "در وجه (گیرنده چک)" else "صادرکننده چک") },
                    isError = isError && partyName.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amountText = it },
                    label = { Text("مبلغ (تومان)") },
                    isError = isError && (amountText.toLongOrNull() ?: 0L) <= 0,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = {
                        val amt = amountText.toLongOrNull() ?: 0L
                        if (amt > 0) {
                            Text("${JalaliCalendar.formatPrice(amt)} تومان")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Due Date
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("تاریخ سررسید (مثال: ۱۴۰۳/۰۶/۱۵)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Bank
                BankDropdown(
                    selectedBank = bankName,
                    onBankSelected = { bankName = it },
                    bankList = IranianBankList.getBankNames(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Sayad ID
                OutlinedTextField(
                    value = sayadNumber,
                    onValueChange = { if (it.length <= 16 && it.all { c -> c.isDigit() }) sayadNumber = it },
                    label = { Text("شناسه ۱۶ رقمی صیادی (اختیاری)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Cheque Number & Branch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chequeNumber,
                        onValueChange = { chequeNumber = it },
                        label = { Text("شماره چک") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = branch,
                        onValueChange = { branch = it },
                        label = { Text("شعبه") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Reminder Days
                OutlinedTextField(
                    value = reminderDays,
                    onValueChange = { if (it.all { c -> c.isDigit() }) reminderDays = it },
                    label = { Text("یادآوری (چند روز قبل از سررسید)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات و بابت (اختیاری)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toLongOrNull() ?: 0L
                    if (partyName.isNotBlank() && amt > 0) {
                        val cheque = (initialCheque ?: Cheque(
                            bankName = bankName,
                            amount = amt,
                            dueDate = dueDate,
                            type = type,
                            partyName = partyName
                        )).copy(
                            type = type,
                            partyName = partyName,
                            amount = amt,
                            dueDate = dueDate,
                            issueDate = issueDate,
                            bankName = bankName,
                            sayadNumber = sayadNumber,
                            chequeNumber = chequeNumber,
                            branch = branch,
                            reminderDaysBefore = reminderDays.toIntOrNull() ?: 3,
                            description = description
                        )
                        onConfirm(cheque)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(if (initialCheque == null) "ثبت چک" else "ذخیره تغییرات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
