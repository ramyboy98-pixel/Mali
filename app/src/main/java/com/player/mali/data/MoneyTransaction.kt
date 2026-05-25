package com.player.mali.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class MoneyTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    val category: String,
    val note: String,
    val paymentMethod: String,
    val dateMillis: Long = System.currentTimeMillis()
)
