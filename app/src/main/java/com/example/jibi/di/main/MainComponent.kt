package com.example.jibi.di.main

import com.example.jibi.ui.app_intro.AppIntroActivity
import com.example.jibi.ui.main.MainActivity
import com.example.jibi.ui.main.transaction.common.MonthManger
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
interface MainComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(): MainComponent
    }

    fun inject(mainActivity: MainActivity)

    fun inject(appIntroActivity: AppIntroActivity)

    val monthManger: MonthManger
}