package com.ssmmhh.jibam.utils

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.ssmmhh.jibam.TestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import org.hamcrest.Description
import org.hamcrest.Matcher


@FlowPreview
@ExperimentalCoroutinesApi
fun getTestBaseApplication(): TestBaseApplication = ApplicationProvider.getApplicationContext()

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
 */
suspend fun ViewInteraction.waitTillViewIsDisplayed(
    timeout: Int = 3_000,
    suspensionPeriod: Int = 100
): ViewInteraction {
    var time = 0
    var wasDisplayed = false
    while (time < timeout) {
        this.withFailureHandler { _: Throwable?, _: Matcher<View?>? ->
            wasDisplayed = false
        }
        this.check(matches(isDisplayed()))
        if (wasDisplayed) {
            return this
        }
        //set it to true if failing handle should set it to false again.
        wasDisplayed = true
        delay(suspensionPeriod.toLong())
        time += suspensionPeriod
        Log.i("isVisible: ViewChecker", "Thread slept for $time milliseconds")
    }
    //after timeOut this will throw isDisplayed exception if view is not still visible
    val additionalErrorInfo = "ViewInteraction.waitTillViewIsDisplayed: We just wait for $this " +
            "to display for $timeout milliseconds but it did not \n moreInfo: "
    this.withFailureHandler { error: Throwable?, _: Matcher<View?>? ->
        throw Throwable(message = additionalErrorInfo + error?.message, error)
    }
    this.check(matches(isDisplayed()))
    return this

}

