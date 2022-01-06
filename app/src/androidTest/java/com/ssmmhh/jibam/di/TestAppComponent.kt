package com.ssmmhh.jibam.di

import android.app.Application
import com.ssmmhh.jibam.di.main.TestMainComponent
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
        SubComponentsModule::class,
        TestModule::class
    ]
)
interface TestAppComponent : AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun testMainComponent(): TestMainComponent.Factory

}
