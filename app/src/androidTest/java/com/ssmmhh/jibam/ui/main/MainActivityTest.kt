package com.ssmmhh.jibam.ui.main

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ssmmhh.jibam.ui.app_intro.AppIntroActivity
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.utils.getSharedPreferencesEditor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * some instrument test for mainActivity
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    @Before
    fun beforeTests() {
        //init the intents to track activities came to screen
        //necessary for intended(hasComponent())
        Intents.init()
    }

    @Test
    fun test_firstRunSoRunAppIntroActivity() {
        //TODO make sure APP_INTRO_PREFERENCE have been set to true
        //launch mainActivity
        ActivityScenario.launch(MainActivity::class.java)
        //check if AppIntroActivity is displayed
        intended(hasComponent(AppIntroActivity::class.java.name))
    }

    private fun changeSharedPrefIsFirstRunValue(newValue: Boolean) {
        getSharedPreferencesEditor().putBoolean(PreferenceKeys.APP_INTRO_PREFERENCE, newValue)
    }
}