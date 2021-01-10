package com.example.jibi.di.main

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.jibi.fragments.main.MainFragmentFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

@Module
object MainFragmentsModule {

    @ExperimentalCoroutinesApi
    @JvmStatic
    @MainScope
    @Provides
    fun provideMainFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        requestManager: RequestManager,
        currentLocal: Locale

    ): FragmentFactory {
        return MainFragmentFactory(
            viewModelFactory, requestManager, currentLocal
        )
    }


}