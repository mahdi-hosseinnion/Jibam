package com.ssmmhh.jibam.endToEndTests

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.data.source.local.dao.TransactionDao
import com.ssmmhh.jibam.MainActivity
import com.ssmmhh.jibam.ui.main.transaction.feature_common.MonthManger
import com.ssmmhh.jibam.util.DateUtils
import com.ssmmhh.jibam.util.EspressoIdlingResources
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.util.separate3By3AndRoundIt
import com.ssmmhh.jibam.utils.createRandomTransaction
import com.ssmmhh.jibam.utils.disableAllPromoteBanners
import com.ssmmhh.jibam.utils.getTestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

/**
 * test transactions page (app first transaction)
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class TransactionsTest {

    @Inject
    lateinit var sharedPrefEditor: SharedPreferences.Editor

    @Inject
    lateinit var transactionsDao: TransactionDao

    @Inject
    lateinit var monthManger: MonthManger

    @Inject
    lateinit var resources: Resources

    @Inject
    lateinit var currentLocale: Locale

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
    fun tryToScrollUpAndDownOnBottomSheet_confirmBackArrowAndHandlerVisibility_withoutAnyTransaction() {
        //confirm back arrow is not visible now but view handler is
        onView(withId(R.id.main_bottom_sheet_back_arrow)).check(matches(not(isDisplayed())))
        onView(withId(R.id.view_hastam)).check(matches(isDisplayed()))
        //swipe up
        onView(withId(R.id.drawer_layout)).perform(swipeUp())
        //confirm back arrow is visible now but view handler is not
        onView(withId(R.id.main_bottom_sheet_back_arrow)).check(matches(isDisplayed()))
        onView(withId(R.id.view_hastam)).check(matches(not(isDisplayed())))
        //swipe down
        onView(withId(R.id.main_standardBottomSheet)).perform(swipeDown())
        //confirm back arrow is not visible now but view handler is
        onView(withId(R.id.main_bottom_sheet_back_arrow)).check(matches(not(isDisplayed())))
        onView(withId(R.id.view_hastam)).check(matches(isDisplayed()))
    }

    @Test
    fun tryToScrollUpAndDownOnBottomSheet_confirmBackArrowAndHandlerVisibility_withRandomTransaction(): Unit =
        runBlocking {
            insertNRandomTransactionsToDbThenReturnAllOfTransactionsInDb(20)
            //confirm back arrow is not visible now but view handler is
            onView(withId(R.id.main_bottom_sheet_back_arrow)).check(matches(not(isDisplayed())))
            onView(withId(R.id.view_hastam)).check(matches(isDisplayed()))
            //swipe up
            onView(withId(R.id.drawer_layout)).perform(swipeUp())
            //confirm back arrow is visible now but view handler is not
            onView(withId(R.id.main_bottom_sheet_back_arrow)).check(matches(isDisplayed()))
            onView(withId(R.id.view_hastam)).check(matches(not(isDisplayed())))

        }

    @Test
    fun shouldShowRightDataOnSummeryMoneySection_afterInsertingFakeTransactions(): Unit =
        runBlocking {
            val insertedTransactions =
                insertNRandomTransactionsToDbThenReturnAllOfTransactionsInDb(20)

            //check summery money section text values
            //confirm incomes has right value
            val sumOfIncomes =
                insertedTransactions.filter { it.money > BigDecimal.ZERO }.sumOf { it.money }
            onView(withId(R.id.txt_income)).check(
                matches(
                    withText(
                        separate3By3AndRoundIt(
                            sumOfIncomes,
                            currentLocale
                        )
                    )
                )
            )
            //confirm expenses has right value
            val sumOfExpenses =
                insertedTransactions.filter { it.money < BigDecimal.ZERO }.sumOf { it.money }
            onView(withId(R.id.txt_expenses)).check(
                matches(
                    withText(
                        separate3By3AndRoundIt(sumOfExpenses.abs(), currentLocale)
                    )
                )
            )
            //confirm balance has right value
            onView(withId(R.id.txt_balance)).check(
                matches(
                    withText(
                        separate3By3AndRoundIt(sumOfIncomes.plus(sumOfExpenses), currentLocale)
                    )
                )
            )
        }

    private suspend fun insertNRandomTransactionsToDbThenReturnAllOfTransactionsInDb(n: Int): List<TransactionDto> {
        //insert 20 random transactions
        repeat(n) {
            val date = Random.nextLong(
                monthManger.getStartOfCurrentMonth(DateUtils.getCurrentUnixTimeInMilliSeconds()),
                monthManger.getEndOfCurrentMonth(DateUtils.getCurrentUnixTimeInMilliSeconds()),
            )
            val transaction = createRandomTransaction(date = date.div(1_000))
            transactionsDao.insertTransaction(transaction)
        }
        return transactionsDao.observeAllOfTransactionsBetweenDates(0, Long.MAX_VALUE, "").first()
    }
}