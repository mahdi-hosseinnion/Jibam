package com.example.jibi.ui.main

import android.os.Bundle
import androidx.fragment.app.FragmentFactory
import com.example.jibi.BaseApplication
import com.example.jibi.R
import com.example.jibi.persistence.RecordsDao
import com.example.jibi.ui.BaseActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MainActivity : BaseActivity() {

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    lateinit var recordsDao: RecordsDao
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