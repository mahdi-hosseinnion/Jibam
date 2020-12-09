package com.example.jibi.util

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.coroutines.Job

class ActiveJobStack<OneShotOperationsStateEvent> : HashMap<String, Job>() {
    private val TAG = "ActiveJobStack: mahdi"

    //for testing
    @IgnoredOnParcel
    //for track how much take to complete one task
    private val jobTiming = HashMap<String, Long>()

    @IgnoredOnParcel
    private val _CountOfActiveJobs: MutableLiveData<Int> = MutableLiveData()

    @IgnoredOnParcel
    val countOfActiveJobs: LiveData<Int>
        get() = _CountOfActiveJobs


    @IgnoredOnParcel
    private val _CountOfUnCancellableJobs: MutableLiveData<Int> = MutableLiveData()

    @IgnoredOnParcel
    //handle nonCancelable jobs
    val unCancellableJobs = ArrayList<OneShotOperationsStateEvent>()
    val countOfUnCancellableJobs: LiveData<Int>
        get() = _CountOfUnCancellableJobs

    override fun put(key: String, value: Job): Job? {
        Log.d(TAG, "put: adding'+++' loading $key")
        jobTiming.put(key, now())
        if (this.containsKey(key)) {
            // prevent duplicate
            return null
        }
        //this method should always called before -> super.put(key, value)
        increaseActiveCount()
        return super.put(key, value)
    }

    override fun remove(key: String): Job? {
        if (this.containsKey(key)) {
            if (jobTiming.containsKey(key)) {
                Log.d(
                    TAG,
                    "put: removing'---' loading $key it took about ${now() - jobTiming.get(key)!!} ms"
                )
            }
            //this method should always called before -> super.remove(key)
            decreaseActiveCount()
            return super.remove(key)
        }
        return null
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

    private fun now() = System.currentTimeMillis()

//    fun checkForUnCancellableJob(stateEvent: OneShotOperationsStateEvent) {
//        if (unCancellableJobs.contains(stateEvent)) {
//            decreaseUnCancellableCount()
//            unCancellableJobs.remove(stateEvent)
//
//        }
//    }
//    fun checkForIsAddedToUnCancellable(stateEvent: OneShotOperationsStateEvent):Boolean {
//        return unCancellableJobs.contains(stateEvent)
//
//    }

    fun addToUnCancellableJob(stateEvent: OneShotOperationsStateEvent) {
        increaseCancellableCount()
        unCancellableJobs.add(stateEvent)
    }

    fun removeFromUnCancellableJob(stateEvent: OneShotOperationsStateEvent) {
        if (unCancellableJobs.contains(stateEvent)) {
            decreaseUnCancellableCount()
            unCancellableJobs.remove(stateEvent)
        }
    }

    private fun increaseCancellableCount() {
        val newValue = (unCancellableJobs.size).plus(1)
        _CountOfUnCancellableJobs.postValue(newValue)
    }

    private fun decreaseUnCancellableCount() {
        val newValue = (unCancellableJobs.size).minus(1)
        _CountOfUnCancellableJobs.postValue(newValue)
    }
}