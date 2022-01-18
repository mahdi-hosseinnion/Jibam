package com.ssmmhh.jibam.di.main

import com.ssmmhh.jibam.endToEndTests.InsertTransactionTest
import com.ssmmhh.jibam.endToEndTests.BasicTest
import com.ssmmhh.jibam.endToEndTests.ViewDetailTransactionTest
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

    fun inject(basicTest: BasicTest)
    fun inject(insertTransactionTest: InsertTransactionTest)
    fun inject(viewDetailTransactionTest: ViewDetailTransactionTest)

}