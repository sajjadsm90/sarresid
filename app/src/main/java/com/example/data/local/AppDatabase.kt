package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Cheque
import com.example.data.model.Installment
import com.example.data.model.Loan

@Database(
    entities = [Cheque::class, Loan::class, Installment::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chequeDao(): ChequeDao
    abstract fun loanDao(): LoanDao
    abstract fun installmentDao(): InstallmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sarresid_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
