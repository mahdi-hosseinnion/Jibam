package com.example.jibi.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.jibi.BaseApplication
import com.example.jibi.R
import com.example.jibi.ui.BaseActivity
import com.example.jibi.ui.main.transaction.BaseTransactionFragment
import com.example.jibi.util.Response
import com.example.jibi.util.StateMessageCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.main_activity_root_view
import kotlinx.android.synthetic.main.fragment_transaction.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        setupActionBarWithNavController()
    }


    override fun inject() {
        (application as BaseApplication).mainComponent()
            .inject(this)
    }

    override fun setupActionBarWithNavController(toolbar: Toolbar) {
//        val toolbar = findViewById<Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.transactionFragment))
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

    }

    override fun onSupportNavigateUp(): Boolean {
        hideSoftKeyboard()
        navController.navigateUp()
        return super.onSupportNavigateUp()
    }



//    override fun hideToolbar() {
//        toolbar_main.visibility = View.GONE
//    }
//
//    override fun showToolbar() {
//        toolbar_main.visibility = View.VISIBLE
//    }

}