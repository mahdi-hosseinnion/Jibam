package com.ssmmhh.jibam.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.ssmmhh.jibam.persistence.AppDatabase
import com.ssmmhh.jibam.persistence.daos.CategoriesDao
import com.ssmmhh.jibam.persistence.daos.TransactionDao
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

@Module(includes = [AppModuleBinds::class])
object AppModule {

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

    @JvmStatic
    @Singleton
    @Provides
    fun provideCategoriesDao(db: AppDatabase): CategoriesDao {
        return db.getCategoriesDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecordsDao(db: AppDatabase): TransactionDao {
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
    //TODO remove resources from dependencies
    @JvmStatic
    @Singleton
    @Provides
    fun provideResources(application: Application): Resources {
        return  application.resources
    }

}
@Module
abstract class AppModuleBinds {

    @Singleton
    @Binds
    abstract fun bindTransactionRepository(transactionRepositoryImpl: TransactionRepositoryImpl)
            : TransactionRepository

    @Singleton
    @Binds
    abstract fun bindCategoryRepository(categoryRepositoryImpl: CategoryRepositoryImpl)
            : CategoryRepository

}