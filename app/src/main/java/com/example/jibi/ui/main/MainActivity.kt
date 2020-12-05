package com.example.jibi.ui.main

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentFactory
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.jibi.BaseApplication
import com.example.jibi.R
import com.example.jibi.persistence.RecordsDao
import com.example.jibi.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@ExperimentalCoroutinesApi
class MainActivity : BaseActivity() {
    lateinit var navController:NavController
    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    lateinit var recordsDao: RecordsDao
    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBarWithNavController()
    }

    override fun inject() {
        (application as BaseApplication).mainComponent()
            .inject(this)
    }

    private fun setupActionBarWithNavController() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.transactionFragment))
        NavigationUI.setupActionBarWithNavController(this,navController,appBarConfiguration)

    }

    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()
        return super.onSupportNavigateUp()
    }
}