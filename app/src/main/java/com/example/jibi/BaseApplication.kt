package com.example.jibi

import android.app.Application
import android.content.Context
import com.example.jibi.di.AppComponent
import com.example.jibi.di.DaggerAppComponent
import com.example.jibi.di.main.MainComponent
import com.example.jibi.util.LocaleHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class BaseApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocaleHelper.onAttachToApplication(base));
    }

    lateinit var appComponent: AppComponent

//    private var authComponent: AuthComponent? = null

    private var mainComponent: MainComponent? = null

    override fun onCreate() {
        super.onCreate()
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

    fun initAppComponent() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }


}