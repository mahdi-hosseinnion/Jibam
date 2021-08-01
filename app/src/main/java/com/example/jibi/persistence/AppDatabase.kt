package com.example.jibi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.models.TransactionEntity

@Database(entities = [Category::class, TransactionEntity::class, CategoryImages::class], version = 4)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getCategoriesDao(): CategoriesDao

    abstract fun getRecordsDao(): RecordsDao

    companion object {
        val DATABASE_NAME: String = "app_db"
    }
}