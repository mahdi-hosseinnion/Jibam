package com.ssmmhh.jibam.endToEndTests

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingRegistry
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ssmmhh.jibam.models.Transaction
import com.ssmmhh.jibam.persistence.RecordsDao
import com.ssmmhh.jibam.ui.main.MainActivity
import com.ssmmhh.jibam.ui.main.transaction.common.MonthManger
import com.ssmmhh.jibam.util.DateUtils
import com.ssmmhh.jibam.util.EspressoIdlingResources
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.utils.createRandomTransaction
import com.ssmmhh.jibam.utils.disableAllPromoteBanners
import com.ssmmhh.jibam.utils.getTestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
    lateinit var recordsDao: RecordsDao

    @Inject
    lateinit var monthManger: MonthManger

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
    fun shouldShowRightDataOnTextViews_afterInsertingFakeTransactions(): Unit = runBlocking {
        insertNRandomTransactionsToDbThenReturnAllOfTransactionsInDb(20)
    }

    private suspend fun insertNRandomTransactionsToDbThenReturnAllOfTransactionsInDb(n: Int): List<Transaction> {
        //insert 20 random transactions
        repeat(n) {
            val date = Random.nextLong(
                monthManger.getStartOfCurrentMonth(DateUtils.getCurrentUnixTimeInMilliSeconds()),
                monthManger.getEndOfCurrentMonth(DateUtils.getCurrentUnixTimeInMilliSeconds()),
            )
            val transaction = createRandomTransaction(date = date.div(1_000).toInt())
            recordsDao.insertOrReplace(transaction)
        }
        return recordsDao.getAllRecords("").first()
    }
}