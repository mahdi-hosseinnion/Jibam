package com.ssmmhh.jibam.ui.main

import android.content.SharedPreferences
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ssmmhh.jibam.TestBaseApplication
import com.ssmmhh.jibam.ui.app_intro.AppIntroActivity
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.utils.getSharedPreferencesEditor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * some instrument test for mainActivity
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    val application: TestBaseApplication = ApplicationProvider.getApplicationContext()

    @Inject
    lateinit var sharedPrefEditor: SharedPreferences.Editor

    init {
        application.testMainComponent()
            .inject(this)
    }

    @Before
    fun beforeTests() {
        //init the intents to track activities came to screen
        //necessary for intended(hasComponent())
        Intents.init()
    }

    @Test
    fun test_firstRunSoRunAppIntroActivity() {
        //set APP_INTRO_PREFERENCE to true, so it means it's the first time app runs
        sharedPrefEditor.putBoolean(PreferenceKeys.APP_INTRO_PREFERENCE, true).commit()
        //launch mainActivity
        ActivityScenario.launch(MainActivity::class.java)
        //check if AppIntroActivity is displayed
        intended(hasComponent(AppIntroActivity::class.java.name))
    }

    private fun changeSharedPrefIsFirstRunValue(newValue: Boolean) {
        getSharedPreferencesEditor().putBoolean(PreferenceKeys.APP_INTRO_PREFERENCE, newValue)
    }
}