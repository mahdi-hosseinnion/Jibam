package com.ssmmhh.jibam.di

import android.app.Application
import androidx.room.Room
import com.ssmmhh.jibam.persistence.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Test fakes of "ProductionModule.kt"
 */
@Module
object TestModule {
    private const val TEST_DATABASE_NAME: String = "test_app_db"

    @JvmStatic
    @Singleton
    @Provides
    fun provideAppDb(app: Application): AppDatabase {
        /**
         * according to: https://medium.com/androiddevelopers/packing-the-room-pre-populate-your-database-with-this-one-method-333ae190e680
         * In memory databases don’t support pre-populating the database via
         * createFromAsset or createFromFile and RoomDatabase.build() method
         * will throw an IllegalArgumentException if you try to ...
         * so we create a normal database then delete it every time that app runs
         * note: b/c tests runs with android orchestrator database will be clear for each test
         */
        //delete old database
        app.deleteDatabase(TEST_DATABASE_NAME)
        //create new one and return it
        return Room
            .databaseBuilder(app, AppDatabase::class.java, TEST_DATABASE_NAME)
            .createFromAsset("databases/categories.db")
            .fallbackToDestructiveMigration()
            .addMigrations(AppDatabase.MIGRATION_4_5)
            .build()
    }
}