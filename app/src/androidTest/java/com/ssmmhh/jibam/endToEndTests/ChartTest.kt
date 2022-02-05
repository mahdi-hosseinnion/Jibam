package com.ssmmhh.jibam.endToEndTests

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.persistence.CategoriesDao
import com.ssmmhh.jibam.persistence.RecordsDao
import com.ssmmhh.jibam.ui.main.MainActivity
import com.ssmmhh.jibam.ui.main.transaction.chart.ChartListAdapter
import com.ssmmhh.jibam.ui.main.transaction.chart.DetailChartListAdapter
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
import java.util.*
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
    lateinit var currentLocale: Locale

    @Inject
    lateinit var categoriesDao: CategoriesDao

    @Inject
    lateinit var recordsDao: RecordsDao

    init {
        //inject this class using dagger
        getTestBaseApplication().mainComponent()
            .inject(this)
    }

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    private val packageName = appContext.packageName

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
        onView(withId(R.id.fab_swap)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.swap_chart)))
            .perform(click())

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
        val transactionsToInsert = TestData.ChartPageTestData.entities.map {
            val currentTime = DateUtils.getCurrentTime()
            it.copy(
                date = Random.nextInt(currentTime.minus(dateRange), currentTime.plus(dateRange))
            )
        }

        val largestExpensesCategoryName = TestData.ChartPageTestData.largestExpensesCategoryName(
            categoriesDao = categoriesDao,
            resources = resources,
            packageName = packageName
        )
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
                    withText(TestData.ChartPageTestData.largestExpensesCategoryMoney),
                    R.id.sumOfMoney
                )
            )
        ).check(
            matches(
                atPositionOnView(
                    0,
                    withText(largestExpensesCategoryName),
                    R.id.category_name
                )
            )
        )

        //click on recyclerView item
        onView(
            withId(R.id.chart_recycler)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<ChartListAdapter.ChartViewHolder>(
                0,
                click()
            )
        )

        //confirm detail chart fragment title
        onView(withId(R.id.topAppBar_normal)).check(
            matches(
                hasDescendant(
                    withText(
                        largestExpensesCategoryName.titleCaseFirstCharIfItIsLowercase()
                    )
                )
            )
        )
        //confirm detail chart recycler items
        val transactionsThatHaveSameCategoryAsLargestOne =
            transactionsToInsert.filter { it.cat_id == TestData.ChartPageTestData.largestExpensesCategoryId }
                .sortedBy { it.money }
        for (item in transactionsThatHaveSameCategoryAsLargestOne) {
            onView(
                withId(R.id.detail_chart_recycler)
            ).check(
                matches(
                    atPositionOnView(
                        transactionsThatHaveSameCategoryAsLargestOne.indexOf(item),
                        withText(item.money.absoluteValue.toString()),
                        R.id.sumOfMoney
                    )
                )
            ).check(
                matches(
                    atPositionOnView(
                        transactionsThatHaveSameCategoryAsLargestOne.indexOf(item),
                        withText(item.memo ?: largestExpensesCategoryName),
                        R.id.category_name
                    )
                )
            )

        }

        //navigate to DetailEditTransactionFragment by clicking on recyclerView items
        onView(
            withId(R.id.detail_chart_recycler)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<DetailChartListAdapter.DetailChartViewHolder>(
                0,
                click()
            )
        )
        //check money amount in detail
        onView(withId(R.id.edt_money))
            .check(matches(withText(transactionsThatHaveSameCategoryAsLargestOne[0].money.absoluteValue.toString())))
        //check memo
        onView(withId(R.id.edt_memo))
            .check(matches(withText(transactionsThatHaveSameCategoryAsLargestOne[0].memo ?: "")))
    }

    @Test
    fun navigateThroughIncomeChartFlow_shouldHaveRightTextValues(): Unit = runBlocking {
        //Arrange
        //insert ten random transaction
        //transactions date should be in this
        val dateRange = 100_000
        val transactionsToInsert = TestData.ChartPageTestData.entities.map {
            val currentTime = DateUtils.getCurrentTime()
            it.copy(
                date = Random.nextInt(currentTime.minus(dateRange), currentTime.plus(dateRange))
            )
        }

        val largestIncomeCategoryName = TestData.ChartPageTestData.largestIncomeCategoryName(
            categoriesDao = categoriesDao,
            resources = resources,
            packageName = packageName
        )
        for (item in transactionsToInsert) {
            recordsDao.insertOrReplace(item)
        }

        //Act
        //click on menu icon
        onView(withContentDescription(R.string.navigation_drawer_cd))
            .perform(click())

        //click on about us item in menu
        onView(withId(R.id.chartFragment)).perform(click())

        onView(withId(R.id.fab_swap)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.swap_chart)))
            .perform(click())

        //confirm recyclerView text
        onView(
            withId(R.id.chart_recycler)
        ).check(
            matches(
                atPositionOnView(
                    0,
                    withText(TestData.ChartPageTestData.largestIncomeCategoryMoney),
                    R.id.sumOfMoney
                )
            )
        ).check(
            matches(
                atPositionOnView(
                    0,
                    withText(largestIncomeCategoryName),
                    R.id.category_name
                )
            )
        )

        //click on recyclerView item
        onView(
            withId(R.id.chart_recycler)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<ChartListAdapter.ChartViewHolder>(
                0,
                click()
            )
        )

        //confirm detail chart fragment title
        onView(withId(R.id.topAppBar_normal)).check(
            matches(
                hasDescendant(
                    withText(
                        largestIncomeCategoryName.titleCaseFirstCharIfItIsLowercase()
                    )
                )
            )
        )
        //confirm detail chart recycler items
        val transactionsThatHaveSameCategoryAsLargestOne =
            transactionsToInsert.filter { it.cat_id == TestData.ChartPageTestData.largestIncomeCategoryId }
                .sortedByDescending { it.money }
        for (item in transactionsThatHaveSameCategoryAsLargestOne) {
            onView(
                withId(R.id.detail_chart_recycler)
            ).check(
                matches(
                    atPositionOnView(
                        transactionsThatHaveSameCategoryAsLargestOne.indexOf(item),
                        withText(item.money.absoluteValue.toString()),
                        R.id.sumOfMoney
                    )
                )
            ).check(
                matches(
                    atPositionOnView(
                        transactionsThatHaveSameCategoryAsLargestOne.indexOf(item),
                        withText(item.memo ?: largestIncomeCategoryName),
                        R.id.category_name
                    )
                )
            )

        }
    }


    /**
     * Replacement for Kotlin's deprecated `capitalize()` function.
     */
    private fun String.titleCaseFirstCharIfItIsLowercase() = replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}