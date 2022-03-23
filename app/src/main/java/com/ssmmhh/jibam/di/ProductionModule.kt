package com.ssmmhh.jibam.di

import android.app.Application
import androidx.room.Room
import com.ssmmhh.jibam.persistence.AppDatabase
import com.ssmmhh.jibam.persistence.AppDatabase.Companion.MIGRATION_4_5
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

/*
    Dependencies in this class have test fakes for ui tests. See "TestModule.kt" in
    androidTest dir
 */
@ExperimentalCoroutinesApi
@FlowPreview
@Module
object ProductionModule {
    @JvmStatic
    @Singleton
    @Provides
    fun provideAppDb(app: Application): AppDatabase {
        return Room
            .databaseBuilder(app, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .createFromAsset("databases/categories.db")
            .fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_4_5)
            .build()
    }
}