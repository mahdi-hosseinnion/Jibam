package com.example.jibi.util

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.coroutines.Job

class ActiveJobStack : HashMap<String, Job>() {
    private val TAG = "MAHDI---MessageStack"

    @IgnoredOnParcel
    protected val _CountOfActiveJobs: MutableLiveData<Int> = MutableLiveData()

    @IgnoredOnParcel
    val countOfActiveJobs: LiveData<Int>
        get() = _CountOfActiveJobs

    override fun put(key: String, value: Job): Job? {
        if (this.containsKey(key)) {
            // prevent duplicate
            return null
        }
        increaseActiveCount()
        return super.put(key, value)
    }

    override fun remove(key: String): Job? {
        if (this.containsKey(key)) {
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
}