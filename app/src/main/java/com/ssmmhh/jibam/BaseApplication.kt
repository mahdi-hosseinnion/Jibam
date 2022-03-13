package com.ssmmhh.jibam

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.ssmmhh.jibam.di.AppComponent
import com.ssmmhh.jibam.di.DaggerAppComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
open class BaseApplication : MultiDexApplication() {

    private lateinit var _appComponent: AppComponent
    open val appComponent get() = _appComponent

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        _appComponent = initAppComponent()
    }

    open fun initAppComponent(): AppComponent = DaggerAppComponent.builder()
        .application(this)
        .build()


}