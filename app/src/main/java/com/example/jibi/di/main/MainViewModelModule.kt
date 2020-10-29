package com.example.jibi.di.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.MainViewModelFactory
import com.example.jibi.di.main.keys.MainViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: MainViewModelFactory): ViewModelProvider.Factory

//    @Binds
//    @IntoMap
//    @MainViewModelKey(AccountViewModel::class)
//    abstract fun bindAccountViewModel(accoutViewModel: AccountViewModel): ViewModel


}