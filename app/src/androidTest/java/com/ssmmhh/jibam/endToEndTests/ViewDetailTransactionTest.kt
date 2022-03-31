package com.ssmmhh.jibam.endToEndTests

import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.data.source.local.dao.CategoriesDao
import com.ssmmhh.jibam.data.source.local.dao.TransactionDao
import com.ssmmhh.jibam.presentation.MainActivity
import com.ssmmhh.jibam.util.DateUtils
import com.ssmmhh.jibam.util.EspressoIdlingResources
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import javax.inject.Inject

/**
 * test view detail screen a new transaction from scratch
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class ViewDetailTransactionTest {

    @Inject
    lateinit var categoriesDao: CategoriesDao

    @Inject
    lateinit var resources: Resources

    @Inject
    lateinit var sharedPrefEditor: SharedPreferences.Editor

    @Inject
    lateinit var transactionsDao: TransactionDao

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    //a global variable to store mainActivity scenario instance to run before each test and close
    //after each test
    private var mainActivityScenario: ActivityScenario<MainActivity>? = null

    init {
        //inject this class using dagger
        getTestBaseApplication().appComponent
            .inject(this)
    }

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
    fun viewDetailOfTransaction_transactionWithOutMemo(): Unit = runBlocking {
        //insert a transaction into the database
        val transactionCategory = categoriesDao.getCategoryById(9)!!.toCategory()
        val transactionMoney = BigDecimal("876.5")
        val tempTransaction = TransactionEntity(
            id = 0,
            money = transactionMoney,
            memo = null,
            cat_id = transactionCategory.id,
            date = DateUtils.getCurrentTime()

        )
        transactionsDao.insertTransaction(tempTransaction)
        //click on item with transactionMemo in recyclerView
        onView(
            withText(
                transactionCategory.getCategoryNameFromStringFile(
                    appContext
                )
            )
        ).perform(click())
        //ViewAssertions

        //check if toolbar has detail value
        onView(withId(R.id.topAppBar_normal)).check(
            matches(
                hasDescendant(
                    withText(
                        resources.getString(
                            R.string.details
                        )
                    )
                )
            )
        )

        //check money amount
        onView(withId(R.id.edt_money))
            .check(matches(withText(transactionMoney.toString())))
        //finalNumber should be empty when clicked on detailTransaction
        onView(withId(R.id.finalNUmber))
            .check(matches(withText("")))
        //category name
        onView(withId(R.id.category_fab)).check(
            matches(
                withText(
                    transactionCategory.getCategoryNameFromStringFile(
                        instrumentationContext
                    )
                )
            )
        )
        //check for category_fab icon
        //note: i already know that icon of category with id '9' is ic_cat_baby
        onView(withId(R.id.category_fab)).check(matches(extendedFAB_withIcon(R.drawable.ic_cat_baby)))
        //TODO check for date and time
        //check memo
        onView(withId(R.id.edt_memo))
            .check(matches(withText("")))

    }

    @Test
    fun viewDetailOfTransaction_transactionWithMemo(): Unit = runBlocking {
        //insert a transaction into the database
        val transactionCategory = categoriesDao.getCategoryById(5)!!.toCategory()
        val transactionMemo = "Hello memo"
        val transactionMoney = BigDecimal("876.5")
        val tempTransaction = TransactionEntity(
            id = 0,
            money = transactionMoney,
            memo = transactionMemo,
            cat_id = transactionCategory.id,
            date = DateUtils.getCurrentTime()

        )
        transactionsDao.insertTransaction(tempTransaction)
        //click on item with transactionMemo in recyclerView
        onView(
            withText(transactionMemo)
        ).perform(click())
        //ViewAssertions

        //check if toolbar has detail value
        onView(withId(R.id.topAppBar_normal)).check(
            matches(
                hasDescendant(
                    withText(
                        resources.getString(
                            R.string.details
                        )
                    )
                )
            )
        )

        //check money amount
        onView(withId(R.id.edt_money))
            .check(matches(withText(transactionMoney.toString())))
        //finalNumber should be empty when clicked on detailTransaction
        onView(withId(R.id.finalNUmber))
            .check(matches(withText("")))
        //category name
        onView(withId(R.id.category_fab)).check(
            matches(
                withText(
                    transactionCategory.getCategoryNameFromStringFile(
                        instrumentationContext
                    )
                )
            )
        )
        //check for category_fab icon
        //note: i already know that icon of category with id '5' is ic_cat_transportation
        onView(withId(R.id.category_fab)).check(matches(extendedFAB_withIcon(R.drawable.ic_cat_transportation)))
        //TODO check for date and time
        //check memo
        onView(withId(R.id.edt_memo))
            .check(matches(withText(transactionMemo)))

    }

    @Test
    fun viewDetailOfTransaction_ThenRemoveIt(): Unit = runBlocking {
        //Arrange
        //insert a transaction into the database
        val transactionCategory = categoriesDao.getCategoryById(5)!!.toCategory()
        val transactionId = 1596
        val tempTransaction = TransactionEntity(
            id = transactionId,
            money = BigDecimal("876.5"),
            memo = null,
            cat_id = transactionCategory.id,
            date = DateUtils.getCurrentTime()

        )
        transactionsDao.insertTransaction(tempTransaction)
        //check if transaction is actully inserted into database
        assert(transactionsDao.getTransactionById(transactionId) != null)
        //click on item with transactionMemo in recyclerView
        onView(
            withText(
                transactionCategory.getCategoryNameFromStringFile(
                    instrumentationContext
                )
            )
        ).check(matches(isDisplayed()))
            .perform(click())
        //ViewAssertions
        //check if toolbar has remove button
        onView(withId(R.id.topAppBar_img_btn))
            .check(matches(isDisplayed()))
            .check(matches(imageViewWithDrawable(R.drawable.ic_round_delete_24)))
        //click on delete image button
        onView(withId(R.id.topAppBar_img_btn)).perform(click())
        //click on dialog yes
        onView(withText(appContext.getString(R.string.text_yes)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())
        //confirm that backed to transaction fragment
        onView(withId(R.id.transaction_toolbar)).check(matches(isDisplayed()))
        //confirm that transaction removed from cache
        assert(transactionsDao.getTransactionById(transactionId) == null)
        Log.d(
            "TESTING",
            "viewDetailOfTransaction_ThenRemoveIt: ${
                transactionsDao.getTransactionById(
                    transactionId
                )
            }"
        )
    }
    //TODO add tests to test edit transaction functionality
}