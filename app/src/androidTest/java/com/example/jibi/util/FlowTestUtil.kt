package com.example.jibi.util

import android.util.TimeUtils
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