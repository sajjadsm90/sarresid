package com.example.data.repository

import com.example.data.local.ChequeDao
import com.example.data.local.InstallmentDao
import com.example.data.local.LoanDao
import com.example.data.model.*
import com.example.util.JalaliCalendar
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val chequeDao: ChequeDao,
    private val loanDao: LoanDao,
    private val installmentDao: InstallmentDao
) {
    // Cheques
    val allCheques: Flow<List<Cheque>> = chequeDao.getAllCheques()

    suspend fun insertCheque(cheque: Cheque): Long = chequeDao.insertCheque(cheque)

    suspend fun updateCheque(cheque: Cheque) = chequeDao.updateCheque(cheque)

    suspend fun deleteCheque(cheque: Cheque) = chequeDao.deleteCheque(cheque)

    suspend fun deleteChequeById(id: Long) = chequeDao.deleteChequeById(id)

    suspend fun updateChequeStatus(id: Long, status: ChequeStatus) = chequeDao.updateStatus(id, status)

    // Loans
    val allLoans: Flow<List<Loan>> = loanDao.getAllLoans()
    val allInstallments: Flow<List<Installment>> = installmentDao.getAllInstallments()

    fun getInstallmentsForLoan(loanId: Long): Flow<List<Installment>> = installmentDao.getInstallmentsForLoan(loanId)

    suspend fun insertLoanWithInstallments(loan: Loan): Long {
        val loanId = loanDao.insertLoan(loan)
        val startDate = JalaliCalendar.parseJalali(loan.startDate) ?: JalaliCalendar.now()
        val installments = mutableListOf<Installment>()

        for (i in 1..loan.totalInstallments) {
            val dueDate = JalaliCalendar.addMonths(startDate, i - 1)
            val dateWithCustomDay = JalaliCalendar.JalaliDate(
                dueDate.year,
                dueDate.month,
                loan.dayOfMonth.coerceAtMost(if (dueDate.month <= 6) 31 else if (dueDate.month <= 11) 30 else 29)
            )
            installments.add(
                Installment(
                    loanId = loanId,
                    installmentNumber = i,
                    amount = loan.installmentAmount,
                    dueDate = dateWithCustomDay.toString(),
                    status = InstallmentStatus.PENDING
                )
            )
        }
        installmentDao.insertInstallments(installments)
        return loanId
    }

    suspend fun deleteLoan(loan: Loan) {
        installmentDao.deleteInstallmentsByLoanId(loan.id)
        loanDao.deleteLoan(loan)
    }

    suspend fun updateInstallmentStatus(id: Long, status: InstallmentStatus, paidDate: String?) {
        installmentDao.updateStatus(id, status, paidDate)
    }
}
