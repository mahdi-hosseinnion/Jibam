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
    val appComponent get() = _appComponent

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initAppComponent()
    }

    open fun initAppComponent() {
        _appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }


}