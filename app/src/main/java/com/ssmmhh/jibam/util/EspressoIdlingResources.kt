package com.ssmmhh.jibam.util

import android.util.Log
import androidx.test.espresso.idling.CountingIdlingResource

object EspressoIdlingResources {
    //TODO PRIVATE THIS
    private const val TAG = "EspressoIdlingResources"
    private const val RESOURCE = "GLOBAL"
    private var count: Int = 0

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment(location: String = "UNKNOWN") {
        count++
        countingIdlingResource.increment()
        Log.d(TAG, "count: $count   +++increment: called from $location ")
    }

    fun decrement(location: String = "UNKNOWN") {
        if (!countingIdlingResource.isIdleNow) {
            count--
            countingIdlingResource.decrement()
            Log.d(TAG, "count: $count   ---decrement: called from $location")
        }
    }
}