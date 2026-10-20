package com.example.data.local

import androidx.room.*
import com.example.data.model.Cheque
import com.example.data.model.ChequeStatus
import com.example.data.model.ChequeType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChequeDao {

    @Query("SELECT * FROM cheques ORDER BY dueDate ASC")
    fun getAllCheques(): Flow<List<Cheque>>

    @Query("SELECT * FROM cheques WHERE type = :type ORDER BY dueDate ASC")
    fun getChequesByType(type: ChequeType): Flow<List<Cheque>>

    @Query("SELECT * FROM cheques WHERE status = :status ORDER BY dueDate ASC")
    fun getChequesByStatus(status: ChequeStatus): Flow<List<Cheque>>

    @Query("SELECT * FROM cheques WHERE id = :id")
    suspend fun getChequeById(id: Long): Cheque?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheque(cheque: Cheque): Long

    @Update
    suspend fun updateCheque(cheque: Cheque)

    @Delete
    suspend fun deleteCheque(cheque: Cheque)

    @Query("DELETE FROM cheques WHERE id = :id")
    suspend fun deleteChequeById(id: Long)

    @Query("UPDATE cheques SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: ChequeStatus)
}
