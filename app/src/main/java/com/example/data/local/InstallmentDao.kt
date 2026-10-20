package com.example.data.local

import androidx.room.*
import com.example.data.model.Installment
import com.example.data.model.InstallmentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentDao {

    @Query("SELECT * FROM installments ORDER BY dueDate ASC")
    fun getAllInstallments(): Flow<List<Installment>>

    @Query("SELECT * FROM installments WHERE loanId = :loanId ORDER BY installmentNumber ASC")
    fun getInstallmentsForLoan(loanId: Long): Flow<List<Installment>>

    @Query("SELECT * FROM installments WHERE status = :status ORDER BY dueDate ASC")
    fun getInstallmentsByStatus(status: InstallmentStatus): Flow<List<Installment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallments(installments: List<Installment>)

    @Update
    suspend fun updateInstallment(installment: Installment)

    @Query("UPDATE installments SET status = :status, paidDate = :paidDate WHERE id = :id")
    suspend fun updateStatus(id: Long, status: InstallmentStatus, paidDate: String?)

    @Query("DELETE FROM installments WHERE loanId = :loanId")
    suspend fun deleteInstallmentsByLoanId(loanId: Long)
}
