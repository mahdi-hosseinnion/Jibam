package com.example.jibi.util

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.coroutines.Job

class ActiveJobStack : HashMap<String, Job>() {
    private val TAG = "ActiveJobStack: mahdi"

    //for testing
    @IgnoredOnParcel
    //for track how much take to complete one task
    private val jobTiming = HashMap<String, Long>()

    @IgnoredOnParcel
    protected val _CountOfActiveJobs: MutableLiveData<Int> = MutableLiveData()

    @IgnoredOnParcel
    val countOfActiveJobs: LiveData<Int>
        get() = _CountOfActiveJobs

    override fun put(key: String, value: Job): Job? {
        Log.d(TAG, "put: adding'+++' loading $key")
        jobTiming.put(key,now())
        if (this.containsKey(key)) {
            // prevent duplicate
            return null
        }
        increaseActiveCount()
        return super.put(key, value)
    }

    override fun remove(key: String): Job? {
        if (this.containsKey(key)) {
            if (jobTiming.containsKey(key)) {
                Log.d(TAG, "put: removing'---' loading $key it took about ${now() - jobTiming.get(key)!!} ms")
            }
            decreaseActiveCount()
            return super.remove(key)
        }
        return null
    }

    override fun clear() {
        clearActiveCount()
        super.clear()
    }

    private fun increaseActiveCount() {
        val currentCount: Int = _CountOfActiveJobs.value ?: 0
        _CountOfActiveJobs.value = currentCount.plus(1)
    }

    private fun decreaseActiveCount() {
        val currentCount: Int = _CountOfActiveJobs.value ?: 0
        _CountOfActiveJobs.value = currentCount.minus(1)
    }

    private fun clearActiveCount() {
        val currentCount: Int = _CountOfActiveJobs.value ?: 0
        _CountOfActiveJobs.value = currentCount.minus(1)
    }
    private fun now() = System.currentTimeMillis()

}