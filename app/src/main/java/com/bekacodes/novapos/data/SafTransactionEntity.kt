package com.bekacodes.novapos.data

import android.content.Context
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saf_transactions")
data class SafTransactionEntity(
    @PrimaryKey val idempotencyKey: String,
    val orderNumber: Int,
    val splitPersonIndex: Int,
    val splitTotalWays: Int,
    val amountChargedUsd: Double,
    val arqcCryptogram: String,
    val aidHex: String = "A0000000031010", // Visa Debit/Credit EMV AID
    val isSynced: Boolean,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Dao
interface NovaPosDao {
    @Query("SELECT COUNT(*) FROM saf_transactions WHERE isSynced = 0")
    fun observePendingSafCount(): Flow<Int>

    @Query("SELECT * FROM saf_transactions WHERE isSynced = 0 ORDER BY createdAtMs ASC")
    suspend fun getPendingSafTransactions(): List<SafTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactionIdempotent(tx: SafTransactionEntity): Long

    @Query("UPDATE saf_transactions SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllPendingAsSynced(): Int

    @Query("SELECT * FROM saf_transactions ORDER BY createdAtMs DESC LIMIT 20")
    fun observeRecentTransactions(): Flow<List<SafTransactionEntity>>
}

@Database(
    entities = [SafTransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NovaPosDatabase : RoomDatabase() {
    abstract fun dao(): NovaPosDao

    companion object {
        @Volatile
        private var INSTANCE: NovaPosDatabase? = null

        fun getInstance(context: Context): NovaPosDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    NovaPosDatabase::class.java,
                    "novapos_saf_ledger.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}