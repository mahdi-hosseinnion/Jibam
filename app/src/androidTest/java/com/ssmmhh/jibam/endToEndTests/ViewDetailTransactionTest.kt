package com.ssmmhh.jibam.endToEndTests

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
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
    fun viewDetailOfTransaction(): Unit = runBlocking {
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
        Espresso.onView(
            ViewMatchers.withText(transactionMemo)
        ).perform(ViewActions.click())
        //ViewAssertions

        //check if toolbar has detail value
        Espresso.onView(ViewMatchers.withId(R.id.topAppBar_normal)).check(
            ViewAssertions.matches(
                ViewMatchers.hasDescendant(
                    ViewMatchers.withText(
                        resources.getString(
                            R.string.details
                        )
                    )
                )
            )
        )

        //check money amount
        Espresso.onView(ViewMatchers.withId(R.id.edt_money))
            .check(ViewAssertions.matches(ViewMatchers.withText(transactionMoney.toString())))
        //finalNumber should be empty when clicked on detailTransaction
        Espresso.onView(ViewMatchers.withId(R.id.finalNUmber))
            .check(ViewAssertions.matches(ViewMatchers.withText("")))
        //category name
        Espresso.onView(ViewMatchers.withId(R.id.category_fab)).check(
            ViewAssertions.matches(
                ViewMatchers.withText(
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
        Espresso.onView(ViewMatchers.withId(R.id.edt_memo))
            .check(ViewAssertions.matches(ViewMatchers.withText(transactionMemo)))

    }
}