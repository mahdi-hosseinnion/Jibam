package com.example.jibi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CategoriesDao::class, RecordsDao::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getCategoriesDao(): CategoriesDao

    abstract fun getRecordsDao(): RecordsDao

    companion object{
        val DATABASE_NAME: String = "app_db"
    }
}