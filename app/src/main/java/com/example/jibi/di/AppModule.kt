package com.example.jibi.di

import android.app.Application
import androidx.room.Room
import com.example.jibi.persistence.AppDatabase
import com.example.jibi.persistence.AppDatabase.Companion.DATABASE_NAME
import com.example.jibi.persistence.CategoriesDao
import com.example.jibi.persistence.RecordsDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {
    @JvmStatic
    @Singleton
    @Provides
    fun provideAppDb(app: Application): AppDatabase {
        return Room
            .databaseBuilder(app, AppDatabase::class.java, DATABASE_NAME)
            .createFromAsset("databases/categories.db")
//            .createFromAsset("databases\\categories.db")
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideCategoriesDao(db: AppDatabase): CategoriesDao {
        return db.getCategoriesDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecordsDao(db: AppDatabase): RecordsDao {
        return db.getRecordsDao()
    }
}