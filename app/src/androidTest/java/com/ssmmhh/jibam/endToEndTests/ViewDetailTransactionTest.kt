package com.ssmmhh.jibam.endToEndTests

import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.models.TransactionEntity
import com.ssmmhh.jibam.persistence.CategoriesDao
import com.ssmmhh.jibam.persistence.RecordsDao
import com.ssmmhh.jibam.ui.main.MainActivity
import com.ssmmhh.jibam.util.DateUtils
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.utils.getTestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * test view detail screen a new transaction from scratch
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class ViewDetailTransactionTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Inject
    lateinit var categoriesDao: CategoriesDao

    @Inject
    lateinit var resources: Resources

    @Inject
    lateinit var sharedPrefEditor: SharedPreferences.Editor

    @Inject
    lateinit var recordsDao: RecordsDao

    private val packageName = appContext.packageName

    init {
        //inject this class using dagger
        getTestBaseApplication().mainComponent()
            .inject(this)
    }

    //a global variable to store mainActivity scenario instance to run before each test and close
    //after each test
    var mainActivityScenario: ActivityScenario<MainActivity>? = null

    @Before
    fun beforeEach() {
        //set APP_INTRO_PREFERENCE to false, so it means user has seen the appIntro and click done
        //therefore mainActivity does not switch to AppIntroActivity
        sharedPrefEditor.putBoolean(PreferenceKeys.APP_INTRO_PREFERENCE, false).commit()
        //launch mainActivity
        mainActivityScenario = ActivityScenario.launch(MainActivity::class.java)

    }

    @After
    fun afterEach() {
        mainActivityScenario?.close()
        mainActivityScenario = null
    }

    @Test
    fun viewDetailOfTransaction_transactionWithOutMemo(): Unit = runBlocking {
        //insert a transaction into the database
        val transactionCategory = categoriesDao.getCategoryById(8)!!
        val transactionMoney = 876.5
        val tempTransaction = TransactionEntity(
            id = 0,
            money = transactionMoney,
            memo = null,
            cat_id = transactionCategory.id,
            date = DateUtils.getCurrentTime()

        )
        recordsDao.insertOrReplace(tempTransaction)
        //click on item with transactionMemo in recyclerView
        onView(
            withText(
                transactionCategory.getCategoryNameFromStringFile(
                    resources,
                    packageName
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
                        resources,
                        appContext.packageName
                    )
                )
            )
        )
        //TODO check for category icon
        //TODO check for date and time
        //check memo
        onView(withId(R.id.edt_memo))
            .check(matches(withText("")))

    }

    @Test
    fun viewDetailOfTransaction_transactionWithMemo(): Unit = runBlocking {
        //insert a transaction into the database
        val transactionCategory = categoriesDao.getCategoryById(5)!!
        val transactionMemo = "Hello memo"
        val transactionMoney = 876.5
        val tempTransaction = TransactionEntity(
            id = 0,
            money = transactionMoney,
            memo = transactionMemo,
            cat_id = transactionCategory.id,
            date = DateUtils.getCurrentTime()

        )
        recordsDao.insertOrReplace(tempTransaction)
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
                        resources,
                        appContext.packageName
                    )
                )
            )
        )
        //TODO check for category icon
        //TODO check for date and time
        //check memo
        onView(withId(R.id.edt_memo))
            .check(matches(withText(transactionMemo)))

    }

    @Test
    fun viewDetailOfTransaction_ThenRemoveIt(): Unit = runBlocking {
        //Arrange
        //insert a transaction into the database
        val transactionCategory = categoriesDao.getCategoryById(5)!!
        val transactionId = 1596
        val tempTransaction = TransactionEntity(
            id = transactionId,
            money = 876.5,
            memo = null,
            cat_id = transactionCategory.id,
            date = DateUtils.getCurrentTime()

        )
        recordsDao.insertOrReplace(tempTransaction)
        //check if transaction is actully inserted into database
        assert(recordsDao.getTransactionById(transactionId) != null)
        //click on item with transactionMemo in recyclerView
        onView(
            withText(
                transactionCategory.getCategoryNameFromStringFile(
                    resources,
                    packageName
                )
            )
        ).check(matches(isDisplayed()))
            .perform(click())
        //ViewAssertions
        //check if toolbar has remove button
        onView(withId(R.id.topAppBar_img_btn)).check(
            matches(isDisplayed())
        )
        //TODO check for icon of imageButton
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
        assert(recordsDao.getTransactionById(transactionId) == null)
        Log.d(
            "TESTING",
            "viewDetailOfTransaction_ThenRemoveIt: ${recordsDao.getTransactionById(transactionId)}"
        )
    }
}