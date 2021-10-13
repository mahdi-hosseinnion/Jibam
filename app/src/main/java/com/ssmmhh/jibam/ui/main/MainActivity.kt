package com.ssmmhh.jibam.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.ssmmhh.jibam.BaseApplication
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.ui.BaseActivity
import com.ssmmhh.jibam.ui.app_intro.AppIntroActivity
import com.ssmmhh.jibam.util.PreferenceKeys
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : BaseActivity() {
    private val TAG = "MainActivity"

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    lateinit var currentLocale: Locale

    lateinit var navController: NavController

    lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var listener: NavController.OnDestinationChangedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            firstSetup()
        }
        uiSetup()
    }

    private fun checkForAppIntro() {
        val isFirstRun = sharedPreferences.getBoolean(
            PreferenceKeys.APP_INTRO_PREFERENCE,
            true
        )
        if (isFirstRun) {
            val intent = Intent(this, AppIntroActivity::class.java)
            startActivity(intent)
        }

    }

    private fun uiSetup() {
//        navigation_view.setupWithNavController(navController)
    }


    override fun inject() {
        (application as BaseApplication).mainComponent()
            .inject(this)
    }

    private fun firstSetup() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        listener =
            NavController.OnDestinationChangedListener { _, _, _ ->
                //disable toolbar for animation
                //hide toolbar during animation
                setSupportActionBar(Toolbar(this))
            }

        appBarConfiguration = AppBarConfiguration(setOf(R.id.transactionFragment))

    }

    var loadingJob: Job? = null

    override fun showProgressBar(isLoading: Boolean) {
        if (isLoading) {
            val transitionAnimTime = resources.getInteger(R.integer.transitionAnimationDuration)
            //if one job takes under 260(transition animation time) we don't want to show progress bar for it
            loadingJob?.cancel()
            loadingJob = null
            loadingJob = lifecycleScope.launch(Main) {
                delay(transitionAnimTime.toLong())
                ensureActive()
                progressBar.visibility = View.VISIBLE
            }
        } else {
            loadingJob?.cancel()
            loadingJob = null
            progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        hideSoftKeyboard()
//        navController.navigateUp()
        return navController.navigateUp(appBarConfiguration) ||
                super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(listener)
        checkForAppIntro()

    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(listener)
    }

//    override fun changeDrawerState(closeIt: Boolean) {
////        if (closeIt)
////            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
////        else
////            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
//    }
//
//    override fun openDrawerMenu() {
////        drawer_layout.open()
//    }
}