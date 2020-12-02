package com.example.jibi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.jibi.models.Category
import com.example.jibi.models.Record

@Database(entities = [Category::class, Record::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getCategoriesDao(): CategoriesDao

    abstract fun getRecordsDao(): RecordsDao

    companion object{
        val DATABASE_NAME: String = "app_db"
    }
}