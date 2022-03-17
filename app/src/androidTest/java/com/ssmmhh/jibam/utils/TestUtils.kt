package com.ssmmhh.jibam.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.ssmmhh.jibam.TestBaseApplication
import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.util.DateUtils
import com.ssmmhh.jibam.util.PreferenceKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import org.hamcrest.Description
import org.hamcrest.Matcher
import java.util.*
import kotlin.math.round
import kotlin.random.Random


@FlowPreview
@ExperimentalCoroutinesApi
fun getTestBaseApplication(): TestBaseApplication = ApplicationProvider.getApplicationContext()

val instrumentationContext : Context get() = InstrumentationRegistry.getInstrumentation().context

fun atPositionOnView(
    position: Int,
    itemMatcher: Matcher<View>,
    targetViewId: Int
): Matcher<View> = object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
    override fun describeTo(description: Description?) {
        description?.appendText("has view id $itemMatcher at position $position")
    }

    override fun matchesSafely(item: RecyclerView?): Boolean {
        val viewHolder: RecyclerView.ViewHolder =
            item?.findViewHolderForAdapterPosition(position) ?: return false
        val targetView = viewHolder.itemView.findViewById<View>(targetViewId)
        return itemMatcher.matches(targetView)
    }


}

/*
    this function freeze the thread till view is displayed
    useCase: after swipe using espresso b/c espresso does not wait for swipe to complete
 */
suspend fun ViewInteraction.waitTillViewIsDisplayed(
    timeout: Int = 3_000,
    suspensionPeriod: Int = 100,
    visibleAreaPercentage: Int = 100
): ViewInteraction {
    var time = 0
    var wasDisplayed = false
    while (time < timeout) {
        this.withFailureHandler { _: Throwable?, _: Matcher<View?>? ->
            wasDisplayed = false
        }
        this.check(matches(isDisplayed()))
        this.check(matches(isDisplayingAtLeast(visibleAreaPercentage)))
        if (wasDisplayed) {
            //set it back to default
            this.withFailureHandler { error, _ -> throw error }
            return this
        }
        //set it to true if failing handle should set it to false again.
        wasDisplayed = true
        delay(suspensionPeriod.toLong())
        time += suspensionPeriod
        Log.i("isVisible: ViewChecker", "Thread slept for $time milliseconds")
    }
    //after timeOut this will throw isDisplayed exception if view is not still visible

    var finalError = Throwable(
        message = "ViewInteraction.waitTillViewIsDisplayed: We just wait for $this " +
                "to display for $timeout milliseconds but it did not \n moreInfo: "
    )
    this.withFailureHandler { error: Throwable?, _: Matcher<View?>? ->
        //catch next checks error
        finalError = Throwable(message = finalError.message + error?.message, error)
    }
    this.check(matches(isDisplayed()))
    this.check(matches(isDisplayingAtLeast(visibleAreaPercentage)))
    throw finalError
}

fun ViewInteraction.getTextFromTextView(): String? {
    var stringHolder: String? = null
    this.perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(TextView::class.java)
        }

        override fun getDescription(): String {
            return "getting text from a TextView"
        }

        override fun perform(uiController: UiController?, view: View) {
            val tv = view as TextView //Save, because of check in getConstraints()
            stringHolder = tv.text.toString()
        }
    })
    return stringHolder
}

/**
 * sometimes ViewActions.swipeLeft does not work on viewPagers so we create a custom one
 */
fun customSwipeLeft(
    swiper: Swiper = Swipe.FAST,
    startCoordinatesProvider: CoordinatesProvider = GeneralLocation.CENTER_RIGHT,
    endCoordinatesProvider: CoordinatesProvider = GeneralLocation.CENTER_LEFT,
    precisionDescriber: PrecisionDescriber = Press.FINGER
): ViewAction? = ViewActions.actionWithAssertions(
    GeneralSwipeAction(
        swiper,
        startCoordinatesProvider,
        endCoordinatesProvider,
        precisionDescriber
    )
)

/**
 * disable all of promotion banner uses to guide user by set all of them to 'false'
 */
fun disableAllPromoteBanners(sharedPrefEditor: SharedPreferences.Editor) {
    sharedPrefEditor.apply {
        putBoolean(PreferenceKeys.PROMOTE_FAB_TRANSACTION_FRAGMENT, false)
        putBoolean(PreferenceKeys.PROMOTE_ADD_TRANSACTION, false)
        putBoolean(PreferenceKeys.PROMOTE_CATEGORY_LIST, false)
        putBoolean(PreferenceKeys.PROMOTE_VIEW_CATEGORY_LIST, false)
        putBoolean(PreferenceKeys.PROMOTE_ADD_CATEGORY_NAME, false)
        putBoolean(PreferenceKeys.PROMOTE_SUMMERY_MONEY, false)
        putBoolean(PreferenceKeys.PROMOTE_MONTH_MANGER, false)
        commit()
    }

}

//Extracted from 'categories.db' located at assets/databases
const val lastExpenseCategoryId = 30
const val lastIncomeCategoryId = 41
fun createRandomTransaction(
    id: Int = 0,
    date: Int = DateUtils.getCurrentTime()
): TransactionEntity {
    val isExpenseTransaction = Random.nextBoolean()

    val randomMoneyAmount = Random.nextDouble(1.0, 1234.0).roundTo(2)
    val randomString = UUID.randomUUID().toString()
        .substring(0, Random.nextInt(20))

    return TransactionEntity(
        id = id,
        money = randomMoneyAmount * (if (isExpenseTransaction) -1 else 1),
        memo = if (Random.nextBoolean()) null else randomString,
        cat_id = if (isExpenseTransaction)
            Random.nextInt(1, lastExpenseCategoryId.plus(1))
        else
            Random.nextInt(lastExpenseCategoryId.plus(1), lastIncomeCategoryId.plus(1)),
        date = date
    )
}

fun Double.roundTo(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}