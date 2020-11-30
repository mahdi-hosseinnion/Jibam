package com.example.jibi.di.main

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
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
        viewModelFactory: ViewModelProvider.Factory
    ): FragmentFactory {
        return MainFragmentFactory(
            viewModelFactory
        )
    }


}