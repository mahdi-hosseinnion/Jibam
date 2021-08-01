package com.example.jibi.util

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope

class ActiveJobStack : ArrayList<String>() {
    private val TAG = "ActiveJobStack: mahdi"

    @IgnoredOnParcel
    private val _CountOfActiveJobs: MutableLiveData<Int> = MutableLiveData()

    @IgnoredOnParcel
    val countOfActiveJobs: LiveData<Int>
        get() = _CountOfActiveJobs

    override fun add(element: String): Boolean {
        if (this.contains(element))
            return false
        increaseActiveCount()
        return super.add(element)
    }

    override fun remove(element: String): Boolean {
        return if (this.contains(element)) {
            decreaseActiveCount()
            super.remove(element)
        } else {
            false
        }
    }

    override fun clear() {
        clearActiveCount()
        super.clear()
    }

    //this method should called beforeChange the size of hashMap
    private fun increaseActiveCount() {
        //we cannot increase the value of mutable liveData by using post value
        //so we use size
        //if you want to increase it immediately by using last value you should use SETVALUE instead of POSTVALUE
        _CountOfActiveJobs.postValue((this.size).plus(1))
        //you should use plus 1 b/c this method called before -> super.put(key, value)
    }

    private fun decreaseActiveCount() {
        //we cannot increase the value of mutable liveData by using post value
        //so we use size
        //if you want to increase it immediately by using last value you should use SETVALUE instead of POSTVALUE
        _CountOfActiveJobs.postValue((this.size).minus(1))
        //you should use minus 1 b/c this method called before -> super.remove(key)
    }

    private fun clearActiveCount() {
        _CountOfActiveJobs.postValue(0)
    }
}