package com.ssmmhh.jibam.di.main

import com.ssmmhh.jibam.ui.main.MainActivityTest
import dagger.Subcomponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@MainScope
@Subcomponent(
    modules = [
        MainModule::class,
        MainViewModelModule::class,
        MainFragmentsModule::class
    ]
)
@ExperimentalCoroutinesApi
interface TestMainComponent : MainComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(): TestMainComponent
    }

    fun inject(mainActivityTest: MainActivityTest)

}