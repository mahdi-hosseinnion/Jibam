package com.ssmmhh.jibam.endToEndTests

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.persistence.RecordsDao
import com.ssmmhh.jibam.ui.main.MainActivity
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.categorybottomsheet.CategoryBottomSheetListAdapter.CategoryViewHolder
import com.ssmmhh.jibam.util.EspressoIdlingResources
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.utils.atPositionOnView
import com.ssmmhh.jibam.utils.getTestBaseApplication
import com.ssmmhh.jibam.utils.repeatTests.Repeat
import com.ssmmhh.jibam.utils.repeatTests.RepeatRule
import com.ssmmhh.jibam.utils.waitTillViewIsDisplayed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import javax.inject.Inject

/**
 * test inserting/adding a new transaction from scratch
 */
//TODO This test don't run mutiple time (i think bug is: espresso does not wait for receiving category list
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class InsertTransactionTest {

    @get:Rule
    val repeatRule: RepeatRule = RepeatRule()

    @Inject
    lateinit var sharedPrefEditor: SharedPreferences.Editor

    @Inject
    lateinit var recordsDao: RecordsDao


    @Inject
    lateinit var resources: Resources

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
    fun insertNewExpensesTransaction_justInsertMoney_checkIfTransactionBeenInserted() {
        //click on 'add_fab' to navigate to insertTransactionFragment
        onView(withId(R.id.add_fab)).perform(click())
        //check if toolbar has add transaction value
        onView(withId(R.id.topAppBar_normal)).check(
            matches(
                hasDescendant(
                    withText(
                        resources.getString(
                            R.string.add_transaction
                        )
                    )
                )
            )
        )
        //check if edit text is in view(we are in addTransactionFragment)
        onView(withId(R.id.edt_money)).check(matches(isDisplayed()))

        //click on first item of categories recyclerView
        onView(
            allOf(
                withId(R.id.main_recycler),
                withParent(withId(R.id.bottom_sheet_viewpager)),//not necessary
                withParentIndex(0)//first position of viewPager
            )
        ).perform(RecyclerViewActions.actionOnItemAtPosition<CategoryViewHolder>(0, click()))

        //insert 123 using calculator buttons
        onView(withId(R.id.btn_1)).perform(click())
        onView(withId(R.id.btn_2)).perform(click())
        onView(withId(R.id.btn_3)).perform(click())

        //click save
        onView(withId(R.id.fab_submit)).perform(click())

        //check if transaction is being inserted and matches number '123'
        //TODO add support for other languages
        onView(withId(R.id.transaction_recyclerView)).check(
            matches(
                atPositionOnView(
                    1,//b/c first position is the header
                    withText("-123"),//negative b/c its expenses transaction
                    R.id.price//id of price textView
                )
            )
        )

    }

    @Test
    fun insertNewIncomeTransaction_justInsertMoney_checkIfTransactionBeenInserted(): Unit =
        runBlocking {
            //click on 'add_fab' to navigate to insertTransactionFragment
            onView(withId(R.id.add_fab)).perform(click())
            //check if toolbar has add transaction value
            onView(withId(R.id.topAppBar_normal)).check(
                matches(
                    hasDescendant(
                        withText(
                            resources.getString(
                                R.string.add_transaction
                            )
                        )
                    )
                )
            )
            //check if edit text is in view(we are in addTransactionFragment)
            onView(withId(R.id.edt_money)).check(matches(isDisplayed()))
            //swap to left (to income page in viewPager)
            onView(withId(R.id.bottom_sheet_viewpager)).perform(swipeLeft())
            //click on first item of categories recyclerView
//            it turns out that espresso does not wait for 'swipeLeft' to complete
//            -stackOverFlow issue: https://stackoverflow.com/q/37294132/10362460
            /**
             * TODO FIX THIS RANDOM BUG
             * if you set 'suspensionPeriod' to 1000 or apply delay before click on recyclerview
             * it will work, so there is bug in swipeLeft
             */
            onView(
                allOf(
                    withId(R.id.main_recycler),
                    withParent(withId(R.id.bottom_sheet_viewpager)),//not necessary
                    withParentIndex(1),//second position of viewPager
                )
            ).waitTillViewIsDisplayed().perform(
                RecyclerViewActions.actionOnItemAtPosition<CategoryViewHolder>(
                    0,
                    click()
                )
            )
            //insert 123 using calculator buttons
            onView(withId(R.id.btn_1)).perform(click())
            onView(withId(R.id.btn_2)).perform(click())
            onView(withId(R.id.btn_3)).perform(click())

            //click save
            onView(withId(R.id.fab_submit)).perform(click())

            //check if transaction is being inserted and matches number '123'
            //TODO add support for other languages
            //TODO check for category icon
            onView(withId(R.id.transaction_recyclerView)).check(
                matches(
                    atPositionOnView(
                        1,//b/c first position is the header
                        withText("123"),//positive b/c its income transaction
                        R.id.price//id of price textView
                    )
                )
            )
        }

    @Test
    fun insertNewExpensesTransaction_insertMoneyAndMemo_checkIfTransactionBeenInserted() {
        //Arrange
        val testMemo = "Test Memo!2"
        //Act
        //click on 'add_fab' to navigate to insertTransactionFragment
        onView(withId(R.id.add_fab)).perform(click())


        //check if toolbar has add transaction value
        onView(withId(R.id.topAppBar_normal)).check(
            matches(
                hasDescendant(
                    withText(
                        resources.getString(
                            R.string.add_transaction
                        )
                    )
                )
            )
        )
        //check if edit text is in view(we are in addTransactionFragment)
        onView(withId(R.id.edt_money)).check(matches(isDisplayed()))

        //click on first item of categories recyclerView
        onView(
            allOf(
                withId(R.id.main_recycler),
                withParent(withId(R.id.bottom_sheet_viewpager)),//not necessary
                withParentIndex(0)//first position of viewPager
            )
        ).perform(RecyclerViewActions.actionOnItemAtPosition<CategoryViewHolder>(0, click()))

        //insert 123 using calculator buttons
        onView(withId(R.id.btn_1)).perform(click())
        onView(withId(R.id.btn_2)).perform(click())
        onView(withId(R.id.btn_3)).perform(click())

        //click on memo editText (get ready for inserting memo)
        onView(withId(R.id.edt_memo)).perform(click())
        //check if calculator keyboard is not displayed
        onView(withId(R.id.keyboard)).check(matches(not(isDisplayed())));
        //insert text into edt_memo
        onView(withId(R.id.edt_memo)).perform(typeText(testMemo))


        //click save
        onView(withId(R.id.fab_submit)).perform(click())

        //check if transaction is being inserted and matches number '123'
        //TODO add support for other languages
        onView(withId(R.id.transaction_recyclerView)).check(
            matches(
                atPositionOnView(
                    1,//b/c first position is the header
                    withText("-123"),//negative b/c its expenses transaction
                    R.id.price//id of price textView
                )
            )
        )
        //check if memo title in recyclerView is Inserted Memo
        onView(withId(R.id.transaction_recyclerView)).check(
            matches(
                atPositionOnView(
                    1,//b/c first position is the header
                    withText(testMemo),
                    R.id.main_text
                )
            )
        )

    }

    @Test
    @Repeat(10)
    fun insertNewIncomeTransaction_insertMoneyAndMemo_checkIfTransactionBeenInserted(): Unit =
        runBlocking {
            //Arrange
            val testMemo = "Test Memo!2"
            //Act
            //click on 'add_fab' to navigate to insertTransactionFragment
            onView(withId(R.id.add_fab)).perform(click())
            //check if toolbar has add transaction value
            onView(withId(R.id.topAppBar_normal)).check(
                matches(
                    hasDescendant(
                        withText(
                            resources.getString(
                                R.string.add_transaction
                            )
                        )
                    )
                )
            )
            //check if edit text is in view(we are in addTransactionFragment)
            onView(withId(R.id.edt_money)).check(matches(isDisplayed()))
            //swap to left (to income page in viewPager)
            onView(withId(R.id.bottom_sheet_viewpager)).perform(swipeLeft())
            //click on first item of categories recyclerView
            //it turns out that espresso does not wait for 'swipeLeft' to complete
            //-stackOverFlow issue: https://stackoverflow.com/q/37294132/10362460
            /**
             * TODO FIX THIS RANDOM BUG
             * if you set 'suspensionPeriod' to 1000 or apply delay before click on recyclerview
             * it will work, so there is bug in swipeLeft
             */
            onView(
                allOf(
                    withId(R.id.main_recycler),
                    withParent(withId(R.id.bottom_sheet_viewpager)),//not necessary
                    withParentIndex(1),//second position of viewPager
                )
            ).waitTillViewIsDisplayed().perform(
                RecyclerViewActions.actionOnItemAtPosition<CategoryViewHolder>(
                    0,
                    click()
                )
            )

            //insert 123 using calculator buttons
            onView(withId(R.id.btn_1)).perform(click())
            onView(withId(R.id.btn_2)).perform(click())
            onView(withId(R.id.btn_3)).perform(click())

            //click on memo editText (get ready for inserting memo)
            onView(withId(R.id.edt_memo)).perform(click())
            //check if calculator keyboard is not displayed
            onView(withId(R.id.keyboard)).check(matches(not(isDisplayed())));
            //insert text into edt_memo
            onView(withId(R.id.edt_memo)).perform(typeText(testMemo))

            //click save
            onView(withId(R.id.fab_submit)).perform(click())
            //Assertions
            //check if transaction is being inserted and matches number '123'
            //TODO add support for other languages
            onView(withId(R.id.transaction_recyclerView)).check(
                matches(
                    atPositionOnView(
                        1,//b/c first position is the header
                        withText("123"),//positive b/c its income transaction
                        R.id.price//id of price textView
                    )
                )
            )
            //check if memo title in recyclerView is Inserted Memo
            onView(withId(R.id.transaction_recyclerView)).check(
                matches(
                    atPositionOnView(
                        1,//b/c first position is the header
                        withText(testMemo),
                        R.id.main_text
                    )
                )
            )
            //TODO check for category icon
        }

}
