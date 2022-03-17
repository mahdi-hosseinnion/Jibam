package com.ssmmhh.jibam.di

import android.app.Application
import com.ssmmhh.jibam.endToEndTests.*
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
@Component(
    modules = [
        AppModule::class,
        TestModule::class,
        MainViewModelModule::class,
        ]
)
interface TestAppComponent : AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
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
