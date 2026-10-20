package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import com.example.notification.NotificationHelper
import com.example.util.BazaarBillingManager
import com.example.util.JalaliCalendar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardSummary(
    val totalReceivableCheques: Long = 0,
    val totalPayableCheques: Long = 0,
    val upcomingChequesCount: Int = 0,
    val upcomingChequesAmount: Long = 0,
    val upcomingInstallmentsCount: Int = 0,
    val upcomingInstallmentsAmount: Long = 0,
    val activeLoansCount: Int = 0,
    val totalRemainingLoanDebt: Long = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val appPreferences: AppPreferences
    val billingManager: BazaarBillingManager = BazaarBillingManager.getInstance(application)

    val cheques: StateFlow<List<Cheque>>
    val loans: StateFlow<List<Loan>>
    val installments: StateFlow<List<Installment>>

    val isPremium: StateFlow<Boolean> = billingManager.isPremium

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _notificationSettings = MutableStateFlow(NotificationSettings())
    val notificationSettings: StateFlow<NotificationSettings> = _notificationSettings.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db.chequeDao(), db.loanDao(), db.installmentDao())
        appPreferences = AppPreferences(application)

        _onboardingCompleted.value = appPreferences.hasCompletedOnboarding
        _notificationSettings.value = NotificationSettings(
            isEnabled = appPreferences.isNotificationEnabled,
            reminderHour = appPreferences.reminderHour,
            reminderMinute = appPreferences.reminderMinute,
            defaultChequeDaysBefore = appPreferences.defaultChequeDaysBefore,
            defaultLoanDaysBefore = appPreferences.defaultLoanDaysBefore
        )

        cheques = repository.allCheques.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        loans = repository.allLoans.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        installments = repository.allInstallments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun canAddCheque(): Boolean {
        return isPremium.value || cheques.value.size < 1
    }

    fun canAddLoan(): Boolean {
        return isPremium.value || loans.value.size < 1
    }

    fun startBazaarPurchase(
        activity: Activity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        billingManager.startPurchaseFlow(activity, onSuccess, onError)
    }

    fun restoreBazaarPurchases(onResult: (Boolean, String) -> Unit) {
        billingManager.restorePurchases(onResult)
    }

    fun setPremiumUnlocked(unlocked: Boolean) {
        billingManager.setPremiumStatus(unlocked)
    }

    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        cheques,
        installments,
        loans
    ) { chequeList, instList, loanList ->
        val todayStr = JalaliCalendar.now().toString()

        val receivable = chequeList
            .filter { it.type == ChequeType.RECEIVABLE && it.status == ChequeStatus.PENDING }
            .sumOf { it.amount }

        val payable = chequeList
            .filter { it.type == ChequeType.PAYABLE && it.status == ChequeStatus.PENDING }
            .sumOf { it.amount }

        val upcomingCheques = chequeList.filter { cheque ->
            if (cheque.status != ChequeStatus.PENDING) return@filter false
            val days = JalaliCalendar.daysBetween(todayStr, cheque.dueDate)
            days in 0..7
        }

        val upcomingInsts = instList.filter { inst ->
            if (inst.status != InstallmentStatus.PENDING) return@filter false
            val days = JalaliCalendar.daysBetween(todayStr, inst.dueDate)
            days in 0..7
        }

        val remainingDebt = instList
            .filter { it.status == InstallmentStatus.PENDING }
            .sumOf { it.amount }

        DashboardSummary(
            totalReceivableCheques = receivable,
            totalPayableCheques = payable,
            upcomingChequesCount = upcomingCheques.size,
            upcomingChequesAmount = upcomingCheques.sumOf { it.amount },
            upcomingInstallmentsCount = upcomingInsts.size,
            upcomingInstallmentsAmount = upcomingInsts.sumOf { it.amount },
            activeLoansCount = loanList.size,
            totalRemainingLoanDebt = remainingDebt
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardSummary()
    )

    fun completeOnboarding() {
        appPreferences.hasCompletedOnboarding = true
        _onboardingCompleted.value = true
    }

    fun addCheque(cheque: Cheque) {
        viewModelScope.launch {
            repository.insertCheque(cheque)
        }
    }

    fun updateCheque(cheque: Cheque) {
        viewModelScope.launch {
            repository.updateCheque(cheque)
        }
    }

    fun deleteCheque(cheque: Cheque) {
        viewModelScope.launch {
            repository.deleteCheque(cheque)
        }
    }

    fun setChequeStatus(id: Long, status: ChequeStatus) {
        viewModelScope.launch {
            repository.updateChequeStatus(id, status)
        }
    }

    fun addLoan(loan: Loan) {
        viewModelScope.launch {
            repository.insertLoanWithInstallments(loan)
        }
    }

    fun deleteLoan(loan: Loan) {
        viewModelScope.launch {
            repository.deleteLoan(loan)
        }
    }

    fun setInstallmentStatus(id: Long, status: InstallmentStatus) {
        viewModelScope.launch {
            val paidDate = if (status == InstallmentStatus.PAID) JalaliCalendar.now().toString() else null
            repository.updateInstallmentStatus(id, status, paidDate)
        }
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        _notificationSettings.value = settings
        appPreferences.isNotificationEnabled = settings.isEnabled
        appPreferences.reminderHour = settings.reminderHour
        appPreferences.reminderMinute = settings.reminderMinute
        appPreferences.defaultChequeDaysBefore = settings.defaultChequeDaysBefore
        appPreferences.defaultLoanDaysBefore = settings.defaultLoanDaysBefore

        if (settings.isEnabled) {
            NotificationHelper.scheduleDailyReminder(
                getApplication(),
                settings.reminderHour,
                settings.reminderMinute
            )
        }
    }
}
