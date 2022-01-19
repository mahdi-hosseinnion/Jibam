package com.ssmmhh.jibam.utils

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.FailureHandler
import androidx.test.espresso.matcher.BoundedMatcher
import com.ssmmhh.jibam.TestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.hamcrest.Description
import org.hamcrest.Matcher
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed

import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers


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
    it turns out that espresso does not waits for 'swipeLeft' to complete
    so it tries to access non visible view
    so we freeze the thread till view is displayed
    -stackOverFlow issue: https://stackoverflow.com/q/37294132/10362460
 */
val TIMEOUT_MILLISECONDS = 5_000
val SLEEP_MILLISECONDS = 100
var time = 0
var wasDisplayed = false

@Throws(InterruptedException::class)
fun isVisible(interaction: ViewInteraction): Boolean? {


    interaction.withFailureHandler { _: Throwable?, _: Matcher<View?>? ->
        wasDisplayed = false
    }
    if (wasDisplayed) {
        time = 0
        wasDisplayed = false
        return true
    }
    if (time >= TIMEOUT_MILLISECONDS) {
        time = 0
        wasDisplayed = false
        return false
    }

    //set it to true if failing handle should set it to false again.
    wasDisplayed = true
    Thread.sleep(SLEEP_MILLISECONDS.toLong())
    time += SLEEP_MILLISECONDS
    interaction.check(matches(isDisplayed()))
    Log.i("ViewChecker", "sleeping")
    return isVisible(interaction)
}
