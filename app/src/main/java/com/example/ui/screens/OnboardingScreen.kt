package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val badgeText: String,
    val accentColor: Color,
    val features: List<String>
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val slides = remember {
        listOf(
            OnboardingSlide(
                title = "خوش‌آمدید به سررسید",
                subtitle = "سامانه هوشمند سررسید چک و اقساط",
                description = "برنامه‌ای جامع و دقیق برای ثبت، محاسبه و یادآوری هوشمند تمامی چک‌های صیادی و اقساط وام‌های بانکی بر اساس تقویم خورشیدی.",
                icon = Icons.Default.EventAvailable,
                badgeText = "اپلیکیشن سررسید",
                accentColor = TealPrimary,
                features = listOf(
                    "پیشخوان زنده با تحلیل وضعیت نقدینگی",
                    "تقویم جلالی دقیق با تفکیک سررسیدها",
                    "پشتیبانی از چک‌های پرداختی و دریافتی"
                )
            ),
            OnboardingSlide(
                title = "یادآور هوشمند و خودکار",
                subtitle = "جلوگیری از برگشت چک و جریمه دیرکرد",
                description = "تنظیم ساعت هشدار و دریافت اعلان‌های دقیق پیش از موعد سررسید برای اطلاع‌رسانی منظم به شما.",
                icon = Icons.Default.NotificationsActive,
                badgeText = "هشدار قبل از سررسید",
                accentColor = AmberWarning,
                features = listOf(
                    "تعیین تعداد روزهای قبل از سررسید برای هشدار",
                    "اطلاع‌رسانی مجزا برای هر قسط یا چک",
                    "ثبت وضعیت چک (وصول شده، برگشتی، جاری)"
                )
            ),
            OnboardingSlide(
                title = "محاسبه‌گر وام و تراز مالی",
                subtitle = "تولید جدول اقساط و گزارش جامع",
                description = "کافیست مبلغ وام و تعداد اقساط را وارد کنید تا جدول کامل اقساط ماهانه ساخته شود و تراز مالی را بررسی کنید.",
                icon = Icons.Default.AccountBalance,
                badgeText = "تراز و گزارش‌گیری",
                accentColor = MintSecondary,
                features = listOf(
                    "ساخت خودکار تمام اقساط یک وام با یک کلیک",
                    "گزارش جامع تراز و قابلیت اشتراک‌گذاری",
                    "دایرکتوری تلفن و کدهای دستوری بانک‌ها"
                )
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 34.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Page Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(slides.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(if (isSelected) 24.dp else 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isSelected) TealPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pagerState.currentPage < slides.size - 1) {
                            TextButton(
                                onClick = onFinish,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("رد شدن", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                            ) {
                                Text("بعدی", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Button(
                                onClick = onFinish,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("start_app_button")
                            ) {
                                Text("شروع و ورود به برنامه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            val slide = slides[page]
            OnboardingSlideContent(slide = slide)
        }
    }
}

@Composable
fun OnboardingSlideContent(slide: OnboardingSlide) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // App Logo Icon Card
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            slide.accentColor.copy(alpha = 0.22f),
                            slide.accentColor.copy(alpha = 0.06f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(slide.accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = slide.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = slide.accentColor.copy(alpha = 0.12f)
        ) {
            Text(
                text = slide.badgeText,
                color = slide.accentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = slide.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = slide.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = slide.accentColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = slide.description,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Feature checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                slide.features.forEach { feature ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = slide.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

