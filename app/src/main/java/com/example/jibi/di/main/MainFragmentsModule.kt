package com.example.jibi.di.main

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.jibi.fragments.main.MainFragmentFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*

@Module
object MainFragmentsModule {

    @FlowPreview
    @ExperimentalCoroutinesApi
    @JvmStatic
    @MainScope
    @Provides
    fun provideMainFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        requestManager: RequestManager,
        currentLocal: Locale,
        sharedPreferences: SharedPreferences,
        sharedPrefEditor: SharedPreferences.Editor,
        resources: Resources
    ): FragmentFactory {
        return MainFragmentFactory(
            viewModelFactory,
            requestManager,
            currentLocal,
            sharedPreferences,
            sharedPrefEditor,
            resources
        )
    }


}