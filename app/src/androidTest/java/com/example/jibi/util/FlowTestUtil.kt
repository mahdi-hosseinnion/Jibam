package com.example.jibi.util

import android.util.TimeUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

////kotlin version of above code
// fun <T> Flow<T>.getOrAwaitValue(
//    time: Long = 2,
//    timeUnit: TimeUnit = TimeUnit.SECONDS
//): T? {
//    var data: T? = null
//    val latch = CountDownLatch(1)
//    this.c
//    this.collect { it ->
//        data = it
//    }
//
//    if (!latch.await(time, timeUnit)) {
//        throw TimeoutException("LiveData value was never set.")
//    }
//    delay(1000)
//    return data
//}
//kotlin version of above code
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)

    afterObserve.invoke()

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        this.removeObserver(observer)
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}