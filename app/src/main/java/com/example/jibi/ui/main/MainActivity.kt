package com.example.jibi.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.jibi.BaseApplication
import com.example.jibi.R
import com.example.jibi.ui.BaseActivity
import com.example.jibi.ui.app_intro.AppIntroActivity
import com.example.jibi.ui.app_intro.ChooseLanguageDialog
import com.example.jibi.util.Constants
import com.example.jibi.util.LocaleHelper
import com.example.jibi.util.PreferenceKeys
import com.example.jibi.util.isFarsi
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : BaseActivity() {
    private val TAG = "MainActivity"

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    lateinit var providerFactory: ViewModelProvider.Factory

    @Inject
    lateinit var currentLocale: Locale

    val viewModel: MainViewModel by viewModels {
        providerFactory
    }

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

    private fun didUserSelectLanguage(): Boolean = sharedPreferences.getBoolean(
        PreferenceKeys.DID_USER_SELECT_LANGUAGE,
        false
    )

    private fun showChooseLangDialog() {
        //TODO ADD OTHER LANGUAGE TO IDN THIS CODE WILL WORK ON OTHER LANGUAGES
        val systemLang = if (currentLocale.isFarsi()) {
            ChooseLanguageDialog.LANGUAGE.PERSIAN
        } else {
            ChooseLanguageDialog.LANGUAGE.ENGLISH
        }
        val interaction = object : ChooseLanguageDialog.Interaction {
            override fun onOkClicked(language: ChooseLanguageDialog.LANGUAGE) {
                userSelectLanguage()
                if (language != systemLang) {
                    changeLanguageTo(language)
                } else {
                    checkForAppIntro()
                }
            }
        }
        val chooseLanguageDialog = ChooseLanguageDialog(
            this, interaction,
            systemLang
        )
        chooseLanguageDialog.show()
    }

    fun userSelectLanguage() {
        sharedPreferences.edit().putBoolean(
            PreferenceKeys.DID_USER_SELECT_LANGUAGE,
            true
        ).apply()
    }

    fun changeLanguageTo(language: ChooseLanguageDialog.LANGUAGE) {
        when (language) {
            ChooseLanguageDialog.LANGUAGE.PERSIAN -> {
                LocaleHelper.setLocale(this, Constants.PERSIAN_LANG_CODE)
            }
            ChooseLanguageDialog.LANGUAGE.ENGLISH -> {
                LocaleHelper.setLocale(this, Constants.ENGLISH_LANG_CODE)
            }
        }
        recreateApp()
    }

    private fun recreateApp() {
        showProgressBar(true)
        lifecycleScope.launch {
            delay(1000)
            showProgressBar(false)
            recreateActivity()
        }
    }

    /*    //TODO REMVOVE THIS ASAP
        private fun checkForRecreate(){
            val shouldRecreate = sharedPreferences.getBoolean(
                PreferenceKeys.SHOULD_RECREATE,
                false
            )
            if (shouldRecreate){
                sharedPreferences.edit().putBoolean(
                    PreferenceKeys.SHOULD_RECREATE,
                    false
                ).commit()
                sharedPreferences.edit().putBoolean(
                    PreferenceKeys.APP_INTRO_PREFERENCE,
                    true
                ).commit()
                val progressdialog = ProgressDialog(this)
                progressdialog.show()
                lifecycleScope.launch {
                    delay(1000)
                    progressdialog.dismiss()
                    recreateActivity()
                }
            }

        }*/
    private fun checkForAppIntro() {
        val isFirstRun = sharedPreferences.getBoolean(
            PreferenceKeys.APP_INTRO_PREFERENCE,
            true
        )
        if (isFirstRun) {
            val intent = Intent(this, AppIntroActivity::class.java)
            startActivity(intent)
        } else {
            splash_screen.visibility = View.GONE
            main_content.visibility = View.VISIBLE
        }

    }

    private fun uiSetup() {
        val menu = navigation_view.menu
        menu.findItem(R.id.chartFragment).title =
            _resources.getString(R.string.chart)

        menu.findItem(R.id.viewCategoriesFragment).title =
            _resources.getString(R.string.category_setting)
        menu.findItem(R.id.aboutUsFragment).title =
            _resources.getString(R.string.about)
        menu.findItem(R.id.settingFragment).title =
            _resources.getString(R.string.setting)

        val header_layout = navigation_view.getHeaderView(0)
        val navHeaderText = header_layout.findViewById<TextView>(R.id.drawer_name)
        navHeaderText.text = _resources.getString(R.string.jibi)

    }


    override fun recreateActivity() {
        val nextIntent = Intent(this, MainActivity::class.java)
        ProcessPhoenix.triggerRebirth(this, nextIntent);
    }

    override fun inject() {
        (application as BaseApplication).mainComponent()
            .inject(this)
    }

    fun firstSetup() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        listener =
            NavController.OnDestinationChangedListener { _, _, _ ->
                //disable toolbar for animation
                //hide toolbar during animation
                setSupportActionBar(Toolbar(this))
            }

        appBarConfiguration = AppBarConfiguration(setOf(R.id.transactionFragment), drawer_layout)


    }

    override fun setupActionBarWithNavController(toolbar: Toolbar, drawerLayout: DrawerLayout?) {
        setSupportActionBar(toolbar)
        navigation_view.setupWithNavController(navController)

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)


    }

    override fun showProgressBar(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
        } else {
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

        if (didUserSelectLanguage()) {
            checkForAppIntro()
        } else {
            showChooseLangDialog()
        }

    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(listener)
    }

    override fun changeDrawerState(closeIt: Boolean) {
        if (closeIt)
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        else
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }
}