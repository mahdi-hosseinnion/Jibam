package com.ssmmhh.jibam

import com.ssmmhh.jibam.di.AppComponent
import com.ssmmhh.jibam.di.DaggerTestAppComponent
import com.ssmmhh.jibam.di.TestAppComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class TestBaseApplication : BaseApplication() {

    override val appComponent: TestAppComponent
        get() = super.appComponent as TestAppComponent

    override fun initAppComponent(): AppComponent =
        DaggerTestAppComponent.builder()
            .application(this)
            .build()

}