package com.ssmmhh.jibam.endToEndTests

import android.content.SharedPreferences
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.ui.app_intro.AppIntroActivity
import com.ssmmhh.jibam.ui.main.MainActivity
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.utils.getTestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * some basic test ex) just run the app
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class BasicTests {

    @Inject
    lateinit var sharedPrefEditor: SharedPreferences.Editor

    init {
        //inject this class using dagger
        getTestBaseApplication().mainComponent()
            .inject(this)
    }

    @Before
    fun beforeEachTest() {
        //remvoe b/c we want to run each test in isolation and
        //if the 'Intents.init()' called two time it will throw error
        Intents.release()
        //init the intents to track activities came to screen
        //necessary for intended(hasComponent())
        Intents.init()
    }

    @Test
    fun shouldOpenAppIntroActivity_whenLaunchMainActivity_IsFirstRunSetToTrue() {
        //set APP_INTRO_PREFERENCE to true, so it means it's the first time app runs
        sharedPrefEditor.putBoolean(PreferenceKeys.APP_INTRO_PREFERENCE, true).commit()
        //launch mainActivity
        ActivityScenario.launch(MainActivity::class.java)
        //check if AppIntroActivity is displayed
        Intents.intended(IntentMatchers.hasComponent(AppIntroActivity::class.java.name))
    }

    @Test
    fun shouldOpenMainActivity_whenLaunchMainActivity_IsFirstRunSetToFalse() {
        //set APP_INTRO_PREFERENCE to false, so it means user has seen the appIntro and click done
        sharedPrefEditor.putBoolean(PreferenceKeys.APP_INTRO_PREFERENCE, false).commit()
        //launch mainActivity
        ActivityScenario.launch(MainActivity::class.java)
        //check with root layout
        onView(withId(R.id.main_activity_root)).check(matches(isDisplayed())) // method #1
        // method #2: this method is completely unnecessary is this situation b/c the method #1 does
        //the work. I just put it here for reminder if i needed it in future
        onView(withId(R.id.main_activity_root)).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )

    }

}