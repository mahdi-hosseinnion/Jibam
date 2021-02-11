package com.example.jibi.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.jibi.BaseApplication
import com.example.jibi.R
import com.example.jibi.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@ExperimentalCoroutinesApi
class MainActivity : BaseActivity() {
    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    lateinit var providerFactory: ViewModelProvider.Factory

    val viewModel: MainViewModel by viewModels {
        providerFactory
    }

    lateinit var navController: NavController

    lateinit var appBarConfiguration:AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBarWithNavController(toolbar_main, drawer_layout)
//        setupActionBarWithNavController()
    }


    override fun inject() {
        (application as BaseApplication).mainComponent()
            .inject(this)
    }

    override fun setupActionBarWithNavController(toolbar: Toolbar, drawerLayout: DrawerLayout?) {
//        val toolbar = findViewById<Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = if (drawerLayout != null) {
            navigation_view.setupWithNavController(navController)
            AppBarConfiguration(setOf(R.id.transactionFragment), drawerLayout)
        }else
            AppBarConfiguration(setOf(R.id.transactionFragment))

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

    }

    override fun onSupportNavigateUp(): Boolean {
        hideSoftKeyboard()
//        navController.navigateUp()
        return navController.navigateUp(appBarConfiguration) ||
                super.onSupportNavigateUp()
    }


//    override fun hideToolbar() {
//        toolbar_main.visibility = View.GONE
//    }
//
//    override fun showToolbar() {
//        toolbar_main.visibility = View.VISIBLE
//    }

}