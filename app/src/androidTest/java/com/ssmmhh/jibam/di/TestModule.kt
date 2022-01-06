package com.ssmmhh.jibam.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.ssmmhh.jibam.persistence.AppDatabase
import com.ssmmhh.jibam.persistence.CategoriesDao
import com.ssmmhh.jibam.persistence.RecordsDao
import com.ssmmhh.jibam.repository.cateogry.CategoryRepository
import com.ssmmhh.jibam.repository.cateogry.CategoryRepositoryImpl
import com.ssmmhh.jibam.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.repository.tranasction.TransactionRepositoryImpl
import com.ssmmhh.jibam.util.PreferenceKeys
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.util.*
import javax.inject.Singleton

@Module(includes = [TestModuleBinds::class])
object TestModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPreferences(
        application: Application
    ): SharedPreferences {
        return application
            .getSharedPreferences(
                PreferenceKeys.APP_MAIN_PREFERENCES,
                Context.MODE_PRIVATE
            )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPrefsEditor(
        sharedPreferences: SharedPreferences
    ): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

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
         */
        //delete old database
        app.deleteDatabase(TEST_DATABASE_NAME)
        //create new one and return it
        return Room
            .databaseBuilder(app, AppDatabase::class.java, TEST_DATABASE_NAME)
            .createFromAsset("databases/categories.db")
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
        //TODO ADD FA/EN for differnet languages in case it didnet work or even _resource
        return ConfigurationCompat.getLocales(app.resources.configuration)[0]

    }

    @Singleton
    @Provides
    fun provideRequestOptions(): RequestOptions {
        return RequestOptions()
//            .error
//            .placeholderOf(R.drawable.ic_cat_others)
//            .error(R.drawable.ic_cat_others)
    }

    @Singleton
    @Provides
    fun provideGlideInstance(
        application: Application,
        requestOptions: RequestOptions
    ): RequestManager {
        return Glide.with(application)
            .setDefaultRequestOptions(requestOptions)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideResources(application: Application): Resources {
        return application.resources
    }

}

@Module
abstract class TestModuleBinds {

    @Singleton
    @Binds
    abstract fun bindTransactionRepository(transactionRepositoryImpl: TransactionRepositoryImpl)
            : TransactionRepository

    @Singleton
    @Binds
    abstract fun bindCategoryRepository(categoryRepositoryImpl: CategoryRepositoryImpl)
            : CategoryRepository

}