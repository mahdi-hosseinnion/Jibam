package com.ssmmhh.jibam.di

import android.app.Application
import com.ssmmhh.jibam.presentation.intro.AppIntroActivity
import com.ssmmhh.jibam.presentation.MainActivity
import com.ssmmhh.jibam.presentation.common.MonthManger
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
    ]
)
@FlowPreview
@ExperimentalCoroutinesApi
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(mainActivity: MainActivity)

    fun inject(appIntroActivity: AppIntroActivity)


}
