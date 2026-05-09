package com.xpenseatlas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT *, (SELECT amount FROM transactions t2 WHERE t2.vendor = t1.vendor AND t2.timestamp < t1.timestamp ORDER BY t2.timestamp DESC LIMIT 1) as previousAmount FROM transactions t1 ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionWithMemory>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getTransactionsForPeriod(startTime: Long): Flow<List<Transaction>>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE isDebit = 1 AND timestamp >= :startTime AND timestamp <= :endTime GROUP BY category ORDER BY total DESC")
    fun getSpendingByCategory(startTime: Long, endTime: Long): Flow<List<CategoryTotal>>

    @Query("SELECT vendor, SUM(amount) as total FROM transactions WHERE isDebit = 1 AND timestamp >= :startTime AND timestamp <= :endTime GROUP BY vendor ORDER BY total DESC LIMIT 3")
    fun getTopMerchants(startTime: Long, endTime: Long): Flow<List<MerchantTotal>>

    @Query("SELECT SUM(amount) FROM transactions WHERE isDebit = 1")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE isDebit = 0")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT vendor, amount, COUNT(*) as count FROM transactions WHERE isDebit = 1 GROUP BY vendor, amount HAVING count > 1 ORDER BY count DESC")
    fun getPossibleSubscriptions(): Flow<List<SubscriptionInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: MerchantMapping)

    @Query("SELECT preferredCategory FROM merchant_mappings WHERE vendor = :vendor LIMIT 1")
    suspend fun getCategoryForVendor(vendor: String): String?

    @Query("SELECT * FROM transactions WHERE vendor = :vendor ORDER BY timestamp DESC LIMIT 1 OFFSET 1")
    suspend fun getPreviousTransactionForMerchant(vendor: String): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}
