package com.ssmmhh.jibam.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.models.CategoryImages
import com.ssmmhh.jibam.models.TransactionEntity

@Database(entities = [Category::class, TransactionEntity::class, CategoryImages::class], version = 4)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getCategoriesDao(): CategoriesDao

    abstract fun getRecordsDao(): RecordsDao

    companion object {
        val DATABASE_NAME: String = "app_db"
    }
}