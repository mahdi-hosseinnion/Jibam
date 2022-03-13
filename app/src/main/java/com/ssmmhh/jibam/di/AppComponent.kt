package com.ssmmhh.jibam.di

import android.app.Application
import com.ssmmhh.jibam.ui.app_intro.AppIntroActivity
import com.ssmmhh.jibam.ui.main.MainActivity
import com.ssmmhh.jibam.ui.main.transaction.common.MonthManger
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ProductionModule::class,
        MainViewModelModule::class,
        MainFragmentFactoryModule::class
    ]
)
@FlowPreview
@ExperimentalCoroutinesApi
interface AppComponent {

    val monthManger: MonthManger

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(mainActivity: MainActivity)

    fun inject(appIntroActivity: AppIntroActivity)


}
