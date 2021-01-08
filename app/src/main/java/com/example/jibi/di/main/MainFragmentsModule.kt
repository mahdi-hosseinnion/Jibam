package com.example.jibi.di.main

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.jibi.fragments.main.MainFragmentFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Module
object MainFragmentsModule {

    @ExperimentalCoroutinesApi
    @JvmStatic
    @MainScope
    @Provides
    fun provideMainFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory, requestManager: RequestManager

    ): FragmentFactory {
        return MainFragmentFactory(
            viewModelFactory, requestManager
        )
    }


}