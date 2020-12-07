package com.example.jibi.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.jibi.BaseApplication
import com.example.jibi.R
import com.example.jibi.ui.BaseActivity
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.util.Response
import com.example.jibi.util.StateEvent
import com.example.jibi.util.StateMessageCallback
import com.example.jibi.util.mahdiLog
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
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

    }

    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()
        return super.onSupportNavigateUp()
    }

    override fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {
        //TODO("Not yet implemented")
    }
    override fun launchNewGlobalJob(stateEvent: StateEvent) {
        if (stateEvent is TransactionStateEvent.OneShotOperationsTransactionStateEvent) {
            mahdiLog("MainActivity", "run new global job: ${stateEvent.getId()}")
            viewModel.launchNewJob(stateEvent = stateEvent)
        }
    }
}