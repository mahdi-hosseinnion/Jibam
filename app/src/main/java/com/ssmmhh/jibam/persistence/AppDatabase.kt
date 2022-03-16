package com.ssmmhh.jibam.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.persistence.entities.CategoryImageEntity
import com.ssmmhh.jibam.persistence.entities.TransactionEntity

@Database(entities = [CategoryEntity::class, TransactionEntity::class, CategoryImageEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getCategoriesDao(): CategoriesDao

    abstract fun getRecordsDao(): RecordsDao

    companion object {
        val DATABASE_NAME: String = "app_db"
    }
}