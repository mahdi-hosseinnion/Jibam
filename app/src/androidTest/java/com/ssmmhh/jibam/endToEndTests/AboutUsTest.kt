package com.ssmmhh.jibam.endToEndTests

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.ui.main.MainActivity
import com.ssmmhh.jibam.util.EspressoIdlingResources
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.utils.getTestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


/**
 * test about us page
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class AboutUsTest {

    @Inject
    lateinit var sharedPrefEditor: SharedPreferences.Editor

    @Inject
    lateinit var resources: Resources

    init {
        //inject this class using dagger
        getTestBaseApplication().mainComponent()
            .inject(this)
    }

    //a global variable to store mainActivity scenario instance to run before each test and close
    //after each test, did not use activityRule b/c APP_INTRO_PREFERENCE should set to false before
    //activity launch
    private var mainActivityScenario: ActivityScenario<MainActivity>? = null

    @Before
    fun beforeEach() {
        //register idling resources
        IdlingRegistry.getInstance().register(EspressoIdlingResources.countingIdlingResource)
        //set APP_INTRO_PREFERENCE to false, so it means user has seen the appIntro and click done
        //therefore mainActivity does not switch to AppIntroActivity
        sharedPrefEditor.putBoolean(PreferenceKeys.APP_INTRO_PREFERENCE, false).commit()
        //launch mainActivity
        mainActivityScenario = ActivityScenario.launch(MainActivity::class.java)

    }

    @After
    fun afterEach() {
        //unregister idling resources
        IdlingRegistry.getInstance().unregister(EspressoIdlingResources.countingIdlingResource)
        mainActivityScenario?.close()
        mainActivityScenario = null
    }

    @Test
    fun confirmDataInAboutUsPage_whenItOpened() {
        //Arrange
        //click on menu icon
        onView(withContentDescription(R.string.navigation_drawer_cd)).perform(click())

        //click on about us item in menu
        onView(withId(R.id.aboutUsFragment)).perform(click())

        //Assertions
        //confirm toolbar title
        onView(withId(R.id.topAppBar_normal)).check(
            ViewAssertions.matches(
                hasDescendant(
                    withText(
                        resources.getString(
                            R.string.about
                        )
                    )
                )
            )
        )
        //confirm textView values
        //TODO Check for background image
        onView(withId(R.id.developement_title)).check(matches(withText(R.string.developement_title)))
        onView(withId(R.id.developement_members)).check(matches(withText(R.string.developement_members)))
        onView(withId(R.id.about_app)).check(matches(withText(R.string.about_app)))
        //hardcore version number to test getVersion function too
        val versionName = "1.0.11"
        onView(withId(R.id.version_name)).check(
            matches(
                withText(
                    resources.getString(R.string.version) + ": $versionName"
                )
            )
        )
    }
}