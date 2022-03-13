package com.ssmmhh.jibam.endToEndTests

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.ui.main.MainActivity
import com.ssmmhh.jibam.util.EspressoIdlingResources
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.util.PreferenceKeys.APP_CALENDAR_PREFERENCE
import com.ssmmhh.jibam.util.PreferenceKeys.CALENDAR_GREGORIAN
import com.ssmmhh.jibam.util.PreferenceKeys.CALENDAR_SOLAR
import com.ssmmhh.jibam.utils.disableAllPromoteBanners
import com.ssmmhh.jibam.utils.getTestBaseApplication
import com.ssmmhh.jibam.utils.getTextFromTextView
import com.ssmmhh.jibam.utils.repeatTests.Repeat
import com.ssmmhh.jibam.utils.repeatTests.RepeatRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject

/**
 * tests for setting page
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class SettingTest {

    @get:Rule
    val repeatRule: RepeatRule = RepeatRule()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var sharedPrefEditor: SharedPreferences.Editor

    @Inject
    lateinit var locale: Locale

    @Inject
    lateinit var resources: Resources

    init {
        //inject this class using dagger
        getTestBaseApplication().appComponent
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
        disableAllPromoteBanners(sharedPrefEditor)
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
    fun shouldShowRightValues_testUIElements() {
        //Arrange
        //navigate to setting fragment(page)
        onView(withContentDescription(R.string.navigation_drawer_cd)).perform(click())
        //click on about us item in menu
        onView(withId(R.id.settingFragment)).perform(click())


        //confirm toolbar title text
        onView(withId(R.id.topAppBar_normal)).check(
            matches(
                hasDescendant(
                    withText(
                        resources.getString(
                            R.string.setting
                        )
                    )
                )
            )
        )
        //confirm views text
        onView(withId(R.id.calender)).check(matches(withText(R.string.calendar)))
        onView(withId(R.id.gregorian_rb)).check(matches(withText(R.string.gregorian)))
        onView(withId(R.id.shamsi_rb)).check(matches(withText(R.string.solar_hijri)))

        //confirm correct radio button is checked
        //receive calendar based on locale
        val calendar = sharedPreferences.getString(
            APP_CALENDAR_PREFERENCE, PreferenceKeys.calendarDefault(locale)
        )
        if (calendar == CALENDAR_SOLAR) {
            onView(withId(R.id.shamsi_rb)).check(matches(isChecked()))
        } else {
            onView(withId(R.id.gregorian_rb)).check(matches(isChecked()))
        }
    }

    @Test
    /**
     * run this test 2 times b/c first time its going to be on locale default calendar
     * next time duo to the sharedPreference value change it will be the opposite of default calendar
     * so we test both scenario and double change scenario
     */
    @Repeat(times = 2)
    fun shouldChangeAppCalendarToShamsi_whenShamsiIsChecked() {
        //Arrange
        //get text of transaction page toolbar month before changing calendar
        val transactionToolbarMonthTextBeforeChangeCalendar: String =
            onView(withId(R.id.toolbar_month)).getTextFromTextView()!!
        //navigate to setting fragment(page)
        onView(withContentDescription(R.string.navigation_drawer_cd)).perform(click())
        //click on about us item in menu
        onView(withId(R.id.settingFragment)).perform(click())

        //ACT
        //receive calendar based on locale
        val calendar = sharedPreferences.getString(
            APP_CALENDAR_PREFERENCE, PreferenceKeys.calendarDefault(locale)
        )
        if (calendar == CALENDAR_SOLAR) {
            onView(withId(R.id.gregorian_rb)).perform(click())
        } else {
            onView(withId(R.id.shamsi_rb)).perform(click())
        }

        //Assert
        //confirm sharedPreference value has changed
        val newCalendar = sharedPreferences.getString(
            APP_CALENDAR_PREFERENCE, PreferenceKeys.calendarDefault(locale)
        )
        assert(calendar != newCalendar)
        if (calendar == CALENDAR_SOLAR)
            assert(newCalendar == CALENDAR_GREGORIAN)
        if (calendar == CALENDAR_GREGORIAN)
            assert(newCalendar == CALENDAR_SOLAR)

        //navigate back to previous fragment(transaction fragment)
        Espresso.pressBack()
        //get text of transaction page toolbar month after changing calendar
        val transactionToolbarMonthTextAfterChangeCalendar: String =
            onView(withId(R.id.toolbar_month)).getTextFromTextView()!!
        //toolbar month should change when calendar type changes
        assert(
            transactionToolbarMonthTextBeforeChangeCalendar !=
                    transactionToolbarMonthTextAfterChangeCalendar
        )

    }
}