package com.ssmmhh.jibam.endToEndTests

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.persistence.CategoriesDao
import com.ssmmhh.jibam.persistence.RecordsDao
import com.ssmmhh.jibam.ui.main.MainActivity
import com.ssmmhh.jibam.util.DateUtils
import com.ssmmhh.jibam.util.EspressoIdlingResources
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.utils.TestData
import com.ssmmhh.jibam.utils.atPositionOnView
import com.ssmmhh.jibam.utils.disableAllPromoteBanners
import com.ssmmhh.jibam.utils.getTestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * test chart and detailChart pages
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class ChartTest {

    @Inject
    lateinit var sharedPrefEditor: SharedPreferences.Editor

    @Inject
    lateinit var resources: Resources

    @Inject
    lateinit var categoriesDao: CategoriesDao

    @Inject
    lateinit var recordsDao: RecordsDao

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

    //    shouldOpenAppIntroActivity_whenLaunchMainActivity_IsFirstRunSetToTrue
    @Test
    fun justNavigateToChartFragment_shouldHaveRightTexts_thenChangeCategoryType() {
        //click on menu icon
        onView(withContentDescription(R.string.navigation_drawer_cd))
            .perform(click())

        //click on about us item in menu
        onView(withId(R.id.chartFragment)).perform(click())

        //Assertion: confirm text values
        //confirm toolbar title
        onView(withId(R.id.topAppBar_month)).check(
            matches(
                hasDescendant(
                    withText(
                        resources.getString(
                            R.string.expenses_chart_title
                        )
                    )
                )
            )
        )
        //confirm toolbar month changer is displayed
        onView(withId(R.id.toolbar_month_changer)).check(matches(isDisplayed()))

        //click on swap chart
        onView(withId(R.id.fab_swap)).check(matches(isDisplayed())).perform(click())

        //confirm that toolbar value has changed to income
        onView(withId(R.id.topAppBar_month)).check(
            matches(
                hasDescendant(
                    withText(
                        resources.getString(
                            R.string.income_chart_title
                        )
                    )
                )
            )
        )
    }

    @Test
    fun navigateThroughExpensesChartFlow_shouldHaveRightTextValues(): Unit = runBlocking {
        //Arrange
        //insert ten random transaction
        //transactions date should be in this
        val dateRange = 100_000
        val transactionsToInsert = TestData.randomTransactionEntities.map {
            val currentTime = DateUtils.getCurrentTime()
            it.copy(
                date = Random.nextInt(currentTime.minus(dateRange), currentTime.plus(dateRange))
            )
        }
        val largestExpensesTransactionMoney =
            transactionsToInsert
                .filter { it.money < 0 }
                .minOf { it.money }//b/c money is negative

        for (item in transactionsToInsert) {
            recordsDao.insertOrReplace(item)
        }

        //Act
        //click on menu icon
        onView(withContentDescription(R.string.navigation_drawer_cd))
            .perform(click())

        //click on about us item in menu
        onView(withId(R.id.chartFragment)).perform(click())
        //confirm recyclerView text
        onView(
            withId(R.id.chart_recycler)
        ).check(
            matches(
                atPositionOnView(
                    0,
                    withText(largestExpensesTransactionMoney.absoluteValue.toString()),
                    R.id.sumOfMoney
                )
            )
        )

    }

}