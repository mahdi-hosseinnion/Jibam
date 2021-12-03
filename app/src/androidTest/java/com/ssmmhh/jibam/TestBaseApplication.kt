package com.ssmmhh.jibam

import com.ssmmhh.jibam.di.DaggerTestAppComponent
import com.ssmmhh.jibam.di.TestAppComponent
import com.ssmmhh.jibam.di.main.MainComponent
import com.ssmmhh.jibam.di.main.TestMainComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class TestBaseApplication : BaseApplication() {

    private var testMainComponent: TestMainComponent? = null

    override fun initAppComponent() {
        appComponent = DaggerTestAppComponent.builder()
            .application(this)
            .build()
    }

    override fun mainComponent(): TestMainComponent {
        if (testMainComponent == null) {
            testMainComponent = (appComponent as TestAppComponent).testMainComponent().create()
        }
        return testMainComponent as TestMainComponent
    }
}