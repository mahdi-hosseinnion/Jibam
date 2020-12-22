package com.example.jibi.di

import android.app.Application
import android.os.Build
import androidx.core.os.ConfigurationCompat
import androidx.room.Room
import com.example.jibi.persistence.AppDatabase
import com.example.jibi.persistence.AppDatabase.Companion.DATABASE_NAME
import com.example.jibi.persistence.CategoriesDao
import com.example.jibi.persistence.RecordsDao
import dagger.Module
import dagger.Provides
import java.util.*
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

    @JvmStatic
    @Singleton
    @Provides
    fun provideCurrentLocal(app: Application): Locale {
        return ConfigurationCompat.getLocales(app.resources.configuration)[0]

    }
}