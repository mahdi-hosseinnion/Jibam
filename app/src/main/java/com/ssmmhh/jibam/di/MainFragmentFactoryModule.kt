package com.ssmmhh.jibam.di

import android.content.SharedPreferences
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.di.factories.MainFragmentFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Singleton

@Module
object MainFragmentFactoryModule {

    @FlowPreview
    @ExperimentalCoroutinesApi
    @JvmStatic
    @Singleton
    @Provides
    fun provideMainFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        requestManager: RequestManager,
        currentLocal: Locale,
        sharedPreferences: SharedPreferences,
        sharedPrefEditor: SharedPreferences.Editor,
    ): FragmentFactory {
        return MainFragmentFactory(
            viewModelFactory,
            requestManager,
            currentLocal,
            sharedPreferences,
            sharedPrefEditor
        )
    }


}