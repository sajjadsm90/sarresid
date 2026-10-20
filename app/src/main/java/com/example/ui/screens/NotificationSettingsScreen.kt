package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.NotificationSettings
import com.example.ui.theme.TealPrimary
import com.example.util.JalaliCalendar

@Composable
fun NotificationSettingsScreen(
    settings: NotificationSettings,
    isPremium: Boolean = false,
    onSaveSettings: (NotificationSettings) -> Unit,
    onUpgradeClick: () -> Unit = {},
    onRestoreClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEnabled by remember { mutableStateOf(settings.isEnabled) }
    var reminderHour by remember { mutableStateOf(settings.reminderHour) }
    var reminderMinute by remember { mutableStateOf(settings.reminderMinute) }
    var defaultChequeDays by remember { mutableStateOf(settings.defaultChequeDaysBefore) }
    var defaultLoanDays by remember { mutableStateOf(settings.defaultLoanDaysBefore) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Premium Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPremium) Color(0xFF004D40) else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = if (!isPremium) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f)) else null
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPremium) Color(0xFFFFD700) else Color(0xFFFFB300).copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = if (isPremium) Color(0xFF3E2723) else Color(0xFFFFA000),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isPremium) "نسخه طلایی فعال است" else "وضعیت اشتراک: نسخه رایگان",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isPremium) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isPremium) "دسترسی نامحدود و مادام‌العمر به همه امکانات" else "محدودیت ۱ چک و ۱ وام فعال است",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isPremium) Color(0xFFFFE082) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (!isPremium) {
                    Button(
                        onClick = onUpgradeClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ارتقا به نسخه طلایی از کافه‌بازار")
                    }
                }

                TextButton(
                    onClick = onRestoreClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isPremium) Color.White else TealPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("بازیابی خرید قبلی از بازار", color = if (isPremium) Color.White else TealPrimary, fontSize = 13.sp)
                }
            }
        }

        // Notification Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "فعال‌سازی اعلان یادآور",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "دریافت هشدار قبل از رسیدن تاریخ سررسید",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Reminder Hour
                Text(
                    text = "ساعت ارسال یادآوری روزانه: ${JalaliCalendar.toPersianDigits(String.format("%02d:%02d", reminderHour, reminderMinute))}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Slider(
                    value = reminderHour.toFloat(),
                    onValueChange = { reminderHour = it.toInt() },
                    valueRange = 6f..22f,
                    steps = 15,
                    enabled = isEnabled
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Default Cheque Days
                Text(
                    text = "پیش‌فرض یادآوری چک: ${JalaliCalendar.toPersianDigits(defaultChequeDays)} روز قبل از سررسید",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Slider(
                    value = defaultChequeDays.toFloat(),
                    onValueChange = { defaultChequeDays = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    enabled = isEnabled
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Default Loan Days
                Text(
                    text = "پیش‌فرض یادآوری اقساط: ${JalaliCalendar.toPersianDigits(defaultLoanDays)} روز قبل از سررسید",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Slider(
                    value = defaultLoanDays.toFloat(),
                    onValueChange = { defaultLoanDays = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    enabled = isEnabled
                )
            }
        }

        Button(
            onClick = {
                onSaveSettings(
                    settings.copy(
                        isEnabled = isEnabled,
                        reminderHour = reminderHour,
                        reminderMinute = reminderMinute,
                        defaultChequeDaysBefore = defaultChequeDays,
                        defaultLoanDaysBefore = defaultLoanDays
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ذخیره تنظیمات اعلان", fontWeight = FontWeight.Bold)
        }
    }
}
