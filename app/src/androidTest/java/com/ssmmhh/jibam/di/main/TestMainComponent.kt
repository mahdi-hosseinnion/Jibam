package com.ssmmhh.jibam.di.main

import com.ssmmhh.jibam.di.MainFragmentFactoryModule
import com.ssmmhh.jibam.di.MainModule
import com.ssmmhh.jibam.di.MainViewModelModule
import com.ssmmhh.jibam.endToEndTests.*
import dagger.Subcomponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@MainScope
@Subcomponent(
    modules = [
        MainModule::class,
        MainViewModelModule::class,
        MainFragmentFactoryModule::class
    ]
)
@ExperimentalCoroutinesApi
interface TestMainComponent : MainComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(): TestMainComponent
    }

    fun inject(basicTest: BasicTest)
    fun inject(insertTransactionTest: InsertTransactionTest)
    fun inject(viewDetailTransactionTest: ViewDetailTransactionTest)
    fun inject(aboutUsTest: AboutUsTest)
    fun inject(settingTest: SettingTest)
    fun inject(categorySettingTest: CategorySettingTest)
    fun inject(chartTest: ChartTest)
    fun inject(transactionsTest: TransactionsTest)

}