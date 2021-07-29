package com.example.jibi.di.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.di.main.keys.MainViewModelKey
import com.example.jibi.ui.main.MainViewModel
import com.example.jibi.viewmodels.MainViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@Module
abstract class MainViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: MainViewModelFactory): ViewModelProvider.Factory

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Binds
    @IntoMap
    @MainViewModelKey(MainViewModel::class)
    abstract fun bindAccountViewModel(mainViewModel: MainViewModel): ViewModel


}