package com.xpenseatlas.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val vendor: String,
    val category: String,
    val timestamp: Long,
    val isDebit: Boolean,
    val currency: String = "₹",
    val isBusiness: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rawSms: String
)

data class CategoryTotal(
    val category: String,
    val total: Double
)

data class MerchantTotal(
    val vendor: String,
    val total: Double
)

data class SubscriptionInfo(
    val vendor: String,
    val amount: Double,
    val count: Int
)

@Entity(tableName = "merchant_mappings")
data class MerchantMapping(
    @PrimaryKey val vendor: String,
    val preferredCategory: String
)

data class TransactionWithMemory(
    @Embedded val transaction: Transaction,
    val previousAmount: Double?
)
