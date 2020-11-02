package com.example.jibi.ui.main

import android.os.Bundle
import com.example.jibi.BaseApplication
import com.example.jibi.R
import com.example.jibi.ui.BaseActivity

class MainActivity : BaseActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun inject() {
        (application as BaseApplication).mainComponent()
            .inject(this)
    }
}