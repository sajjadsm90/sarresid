package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.AddChequeDialog
import com.example.ui.dialogs.AddLoanDialog
import com.example.ui.dialogs.PremiumUpgradeDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

enum class NavigationItem(val title: String) {
    DASHBOARD("پیشخوان"),
    CALENDAR("تقویم"),
    CHEQUES("چک‌ها"),
    INSTALLMENTS("اقساط"),
    REPORTS("گزارش‌ها"),
    BANKS("بانک‌ها"),
    SETTINGS("تنظیمات")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            SarresidTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()

                    if (!onboardingCompleted) {
                        OnboardingScreen(
                            onFinish = { viewModel.completeOnboarding() }
                        )
                    } else {
                        MainScreenContent(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(viewModel: MainViewModel) {
    var currentNav by remember { mutableStateOf(NavigationItem.DASHBOARD) }
    var showAddChequeDialog by remember { mutableStateOf(false) }
    var prefilledChequeDate by remember { mutableStateOf<String?>(null) }
    var showAddLoanDialog by remember { mutableStateOf(false) }
    var showPremiumUpgradeDialog by remember { mutableStateOf(false) }

    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val cheques by viewModel.cheques.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val installments by viewModel.installments.collectAsStateWithLifecycle()
    val notifSettings by viewModel.notificationSettings.collectAsStateWithLifecycle()

    val handleAddChequeRequest: (String?) -> Unit = { date ->
        if (viewModel.canAddCheque()) {
            prefilledChequeDate = date
            showAddChequeDialog = true
        } else {
            showPremiumUpgradeDialog = true
        }
    }

    val handleAddLoanRequest: () -> Unit = {
        if (viewModel.canAddLoan()) {
            showAddLoanDialog = true
        } else {
            showPremiumUpgradeDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = if (currentNav == NavigationItem.SETTINGS) "تنظیمات یادآور" else if (currentNav == NavigationItem.BANKS) "اطلاعات بانک‌ها" else "سررسید",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    if (currentNav == NavigationItem.SETTINGS || currentNav == NavigationItem.BANKS) {
                        IconButton(onClick = { currentNav = NavigationItem.DASHBOARD }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "بازگشت")
                        }
                    }
                },
                actions = {
                    if (currentNav != NavigationItem.BANKS && currentNav != NavigationItem.SETTINGS) {
                        // VIP Golden Button / Badge
                        Surface(
                            onClick = { showPremiumUpgradeDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isPremium) Color(0xFFFFD700).copy(alpha = 0.2f) else Color(0xFFFFB300),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = if (isPremium) Color(0xFFFFA000) else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isPremium) "VIP" else "طلایی",
                                    color = if (isPremium) Color(0xFFFFA000) else Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(onClick = { currentNav = NavigationItem.BANKS }) {
                            Icon(Icons.Default.AccountBalance, contentDescription = "بانک‌ها", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { currentNav = NavigationItem.SETTINGS }) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = "تنظیمات یادآور", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = {
                            handleAddChequeRequest(null)
                        }) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "افزودن چک", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentNav == NavigationItem.DASHBOARD,
                    onClick = { currentNav = NavigationItem.DASHBOARD },
                    icon = { Icon(Icons.Default.Home, contentDescription = "پیشخوان") },
                    label = { Text("پیشخوان") },
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = currentNav == NavigationItem.CALENDAR,
                    onClick = { currentNav = NavigationItem.CALENDAR },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "تقویم") },
                    label = { Text("تقویم") },
                    modifier = Modifier.testTag("nav_calendar")
                )
                NavigationBarItem(
                    selected = currentNav == NavigationItem.CHEQUES,
                    onClick = { currentNav = NavigationItem.CHEQUES },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "چک‌ها") },
                    label = { Text("چک‌ها") },
                    modifier = Modifier.testTag("nav_cheques")
                )
                NavigationBarItem(
                    selected = currentNav == NavigationItem.INSTALLMENTS,
                    onClick = { currentNav = NavigationItem.INSTALLMENTS },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "اقساط") },
                    label = { Text("اقساط") },
                    modifier = Modifier.testTag("nav_installments")
                )
                NavigationBarItem(
                    selected = currentNav == NavigationItem.REPORTS,
                    onClick = { currentNav = NavigationItem.REPORTS },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "گزارش‌ها") },
                    label = { Text("گزارش‌ها") },
                    modifier = Modifier.testTag("nav_reports")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentNav) {
                NavigationItem.DASHBOARD -> DashboardScreen(
                    summary = summary,
                    cheques = cheques,
                    installments = installments,
                    loans = loans,
                    isPremium = isPremium,
                    onAddChequeClick = { handleAddChequeRequest(null) },
                    onAddLoanClick = handleAddLoanRequest,
                    onNavigateToCheques = { currentNav = NavigationItem.CHEQUES },
                    onNavigateToInstallments = { currentNav = NavigationItem.INSTALLMENTS },
                    onUpgradeClick = { showPremiumUpgradeDialog = true }
                )
                NavigationItem.CALENDAR -> CalendarScreen(
                    cheques = cheques,
                    installments = installments,
                    loans = loans,
                    onAddChequeWithDate = { dateStr -> handleAddChequeRequest(dateStr) },
                    onAddLoanClick = handleAddLoanRequest,
                    onUpdateChequeStatus = { id, status -> viewModel.setChequeStatus(id, status) },
                    onUpdateInstallmentStatus = { id, status -> viewModel.setInstallmentStatus(id, status) }
                )
                NavigationItem.CHEQUES -> ChequesScreen(
                    cheques = cheques,
                    isPremium = isPremium,
                    onRequestAddCheque = { handleAddChequeRequest(null) },
                    onAddCheque = { viewModel.addCheque(it) },
                    onUpdateCheque = { viewModel.updateCheque(it) },
                    onDeleteCheque = { viewModel.deleteCheque(it) },
                    onUpdateStatus = { id, status -> viewModel.setChequeStatus(id, status) },
                    onUpgradeClick = { showPremiumUpgradeDialog = true }
                )
                NavigationItem.INSTALLMENTS -> InstallmentsScreen(
                    loans = loans,
                    installments = installments,
                    isPremium = isPremium,
                    onRequestAddLoan = handleAddLoanRequest,
                    onAddLoan = { viewModel.addLoan(it) },
                    onDeleteLoan = { viewModel.deleteLoan(it) },
                    onUpdateInstallmentStatus = { id, status -> viewModel.setInstallmentStatus(id, status) },
                    onUpgradeClick = { showPremiumUpgradeDialog = true }
                )
                NavigationItem.REPORTS -> ReportsScreen(
                    cheques = cheques,
                    loans = loans,
                    installments = installments
                )
                NavigationItem.BANKS -> BanksDirectoryScreen()
                NavigationItem.SETTINGS -> NotificationSettingsScreen(
                    settings = notifSettings,
                    isPremium = isPremium,
                    onSaveSettings = { viewModel.updateNotificationSettings(it) },
                    onUpgradeClick = { showPremiumUpgradeDialog = true },
                    onRestoreClick = { showPremiumUpgradeDialog = true }
                )
            }
        }
    }

    if (showAddChequeDialog) {
        AddChequeDialog(
            initialDueDate = prefilledChequeDate,
            onDismiss = {
                showAddChequeDialog = false
                prefilledChequeDate = null
            },
            onConfirm = {
                viewModel.addCheque(it)
                showAddChequeDialog = false
                prefilledChequeDate = null
            }
        )
    }

    if (showAddLoanDialog) {
        AddLoanDialog(
            onDismiss = { showAddLoanDialog = false },
            onConfirm = {
                viewModel.addLoan(it)
                showAddLoanDialog = false
            }
        )
    }

    if (showPremiumUpgradeDialog) {
        PremiumUpgradeDialog(
            currentChequesCount = cheques.size,
            currentLoansCount = loans.size,
            onDismiss = { showPremiumUpgradeDialog = false },
            onPurchaseClick = { act ->
                viewModel.startBazaarPurchase(
                    activity = act,
                    onSuccess = {
                        showPremiumUpgradeDialog = false
                    },
                    onError = { _ -> }
                )
            },
            onRestoreClick = { callback ->
                viewModel.restoreBazaarPurchases(callback)
            },
            onTestUnlock = { unlocked ->
                viewModel.setPremiumUnlocked(unlocked)
            }
        )
    }
}
