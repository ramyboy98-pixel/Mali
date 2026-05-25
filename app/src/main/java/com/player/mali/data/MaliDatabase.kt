package com.player.mali.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MoneyTransaction::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MaliDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: MaliDatabase? = null

        fun get(context: Context): MaliDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MaliDatabase::class.java,
                    "mali_database.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
