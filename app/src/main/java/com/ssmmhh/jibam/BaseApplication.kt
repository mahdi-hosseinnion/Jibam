package com.ssmmhh.jibam

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.ssmmhh.jibam.di.AppComponent
import com.ssmmhh.jibam.di.DaggerAppComponent
import com.ssmmhh.jibam.di.main.MainComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
open class BaseApplication : MultiDexApplication() {

    lateinit var appComponent: AppComponent

//    private var authComponent: AuthComponent? = null

    private var mainComponent: MainComponent? = null

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initAppComponent()
    }

    fun releaseMainComponent() {
        mainComponent = null
    }

    fun mainComponent(): MainComponent {
        if (mainComponent == null) {
            mainComponent = appComponent.mainComponent().create()
        }
        return mainComponent as MainComponent
    }

//    fun releaseAuthComponent(){
//        authComponent = null
//    }

//    fun authComponent(): AuthComponent {
//        if(authComponent == null){
//            authComponent = appComponent.authComponent().create()
//        }
//        return authComponent as AuthComponent
//    }

    open fun initAppComponent() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }


}