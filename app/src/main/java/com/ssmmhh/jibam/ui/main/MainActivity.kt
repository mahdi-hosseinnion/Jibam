package com.ssmmhh.jibam.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.ssmmhh.jibam.BaseApplication
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.ActivityMainBinding
import com.ssmmhh.jibam.di.factories.MainFragmentFactory
import com.ssmmhh.jibam.persistence.daos.TransactionDao
import com.ssmmhh.jibam.ui.BaseActivity
import com.ssmmhh.jibam.ui.app_intro.AppIntroActivity
import com.ssmmhh.jibam.ui.main.transaction.common.MonthManger
import com.ssmmhh.jibam.util.PreferenceKeys
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : BaseActivity() {
    private val TAG = "MainActivity"

    @Inject
    lateinit var fragmentFactory: MainFragmentFactory

    @Inject
    lateinit var currentLocale: Locale
    
    @Inject
    lateinit var  monthManager:MonthManger
    
    @Inject
    lateinit var transactionDao: TransactionDao
    
    lateinit var navController: NavController

    lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var listener: NavController.OnDestinationChangedListener

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        setFragmentFactory()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        if (savedInstanceState == null) {
            firstSetup()
        }
        uiSetup()
        lifecycleScope.launchWhenCreated {
            monthManager.currentMonth.collect {
                 transactionDao.aaaaaaaaaaaaaaaaaaaaaaaa(
                    minDate = it.startOfMonth,
                    maxDate = it.endOfMonth
                ).collect {
                     Log.d(TAG, "onCreate: value: $it")

                 }
            }
            
        }
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
        (application as BaseApplication).appComponent.inject(this)
    }

    private fun setFragmentFactory() {
        supportFragmentManager.fragmentFactory = fragmentFactory
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
                binding.progressBar.visibility = View.VISIBLE
            }
        } else {
            loadingJob?.cancel()
            loadingJob = null
            binding.progressBar.visibility = View.INVISIBLE
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