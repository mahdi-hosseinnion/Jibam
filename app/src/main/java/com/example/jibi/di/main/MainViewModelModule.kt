package com.example.jibi.di.main

import androidx.lifecycle.ViewModelProvider
import com.example.jibi.viewmodels.MainViewModelFactory
import dagger.Binds
import dagger.Module

@Module
abstract class MainViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: MainViewModelFactory): ViewModelProvider.Factory

//    @Binds
//    @IntoMap
//    @MainViewModelKey(AccountViewModel::class)
//    abstract fun bindAccountViewModel(accoutViewModel: AccountViewModel): ViewModel


}