package com.ssmmhh.jibam.di

import android.app.Application
import com.ssmmhh.jibam.di.main.MainComponent
import com.ssmmhh.jibam.ui.BaseActivity
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        SubComponentsModule::class
    ]
)
@ExperimentalCoroutinesApi
interface AppComponent {

//    val sessionManager: SessionManager

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(baseActivity: BaseActivity)

//    fun authComponent(): AuthComponent.Factory

    fun mainComponent(): MainComponent.Factory

}
