package com.example.jibi.ui

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.jibi.BaseApplication

abstract class BaseActivity : AppCompatActivity() {

    abstract fun inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApplication).appComponent
            .inject(this)
        super.onCreate(savedInstanceState)
    }
}