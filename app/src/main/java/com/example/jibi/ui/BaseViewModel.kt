package com.example.jibi.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

abstract class BaseViewModel<OneShotOperationsStateEvent, ViewState> : ViewModel() {

    val TAG: String = "BaseViewModel"

    //    protected val _stateEvent: MutableSharedFlow<StateEvent> = MutableSharedFlow()
    private val _viewState: MutableLiveData<ViewState> = MutableLiveData()
    private val _messageStack = MessageStack()
    private val _activeJobStack = ActiveJobStack<OneShotOperationsStateEvent>()

    //handle nonCancelable jobs
    fun runPendingJobs() {
        mahdiLog(TAG, "run pending jobs:${countOfNonCancellableJobs.value}")
        if ((countOfNonCancellableJobs.value ?: 0) > 0) {
            for (item in _activeJobStack.unCancellableJobs) {
                _activeJobStack.removeFromUnCancellableJob(item)
                launchNewJob(item)
            }
        }
    }

    private val handler = CoroutineExceptionHandler { _, throwable ->
        //should we remove job from _activeJobStack?
        addToMessageStack(throwable = throwable)
    }

    val viewState: LiveData<ViewState>
        get() = _viewState

    val stateMessage: LiveData<StateMessage?>
        get() = _messageStack.stateMessage

    //    val loading: LiveData<Boolean> = liveData { TODO("OR")
    val countOfActiveJobs: LiveData<Int>
        get() = _activeJobStack.countOfActiveJobs

    val countOfNonCancellableJobs: LiveData<Int>
        get() = _activeJobStack.countOfUnCancellableJobs


    fun launchNewJob(stateEvent: OneShotOperationsStateEvent, isUnCancellable: Boolean = false) {
        if (stateEvent !is StateEvent) {
            addToMessageStack("Unknown State Event")
            Log.e(TAG, "launchNewJob: YOU FORGOT TO EXTEND FROM STATE EVENT")
            return
        }

        if (_activeJobStack.containsKey(stateEvent.getId())) {
            //if already job is active
            return
        }

        val job = viewModelScope.launch(IO + handler) {

            ensureActive()

            val dataState = getResultByStateEvent(stateEvent)

            ensureActive()
            withContext(Main) {
                handleNewDataState(dataState)


            }
        }
        //add job to active job stack
        _activeJobStack.put(stateEvent.getId(), job)

        job.invokeOnCompletion { throwable ->
            _activeJobStack.remove(stateEvent.getId())
            //handle nonCancelable jobs

            if (throwable == null) {
                mahdiLog(TAG, "launchNewJob: Job: completed normally  ${stateEvent.getId()}")
                return@invokeOnCompletion
            }
            if (throwable is CancellationException) {
                if (isUnCancellable) {
                    //handle nonCancelable jobs
                    mahdiLog(
                        TAG,
                        "launchNewJob: Job: added to unCancelable jobs stack ${stateEvent.getId()} msg: ${throwable.message} cuze: ${throwable.cause?.message}"
                    )
                    _activeJobStack.addToUnCancellableJob(stateEvent)
                    mahdiLog(
                        TAG,
                        "the unCancelable size is: ${_activeJobStack.unCancellableJobs.size}"
                    )
                    mahdiLog(
                        TAG,
                        "the unCancelable liveData size is: ${countOfNonCancellableJobs.value}"
                    )
                } else {
                    mahdiLog(
                        TAG,
                        "launchNewJob: Job: cancelled normally  ${stateEvent.getId()} msg: ${throwable.message} cuze: ${throwable.cause?.message} , $throwable , ${throwable.cause}"
                    )
                }
                return@invokeOnCompletion
            }
            addToMessageStack(throwable = throwable)
        }
    }

    fun addToMessageStack(
        message: String? = null,
        throwable: Throwable? = null,
        uiComponentType: UIComponentType = UIComponentType.Toast,
        messageType: MessageType = MessageType.Error
    ) {
        if (message == null && throwable == null) {
            return
        }
        var mMessage = message
        if (mMessage == null && throwable != null) {
            mMessage = throwable.message
        }
        Log.e(TAG, "addToMessageStack: error: $mMessage ", throwable)
        _messageStack.add(
            StateMessage(
                Response(
                    message = mMessage,
                    uiComponentType = uiComponentType,
                    messageType = messageType
                )
            )
        )

    }

    private suspend fun handleNewDataState(dataState: DataState<ViewState>) {
        withContext(Main) {
            ensureActive()
            //        handleStateEvent(stateEvent).onEach{ dataState -> TODO("OR")
            dataState.data?.let { viewState ->
//                        it.getContentIfNotHandled()?.let { viewState ->TODO ("OR")
                ensureActive()
                handleNewData(viewState)
            }
            dataState.stateMessage.let { stateMessage ->
//                        it.getContentIfNotHandled()?.let { viewState ->TODO ("OR")
                stateMessage?.let {
                    ensureActive()
                    _messageStack.add(it)
                }

            }
            ensureActive()
        }
    }

    fun areAnyJobsActive(): Boolean = _activeJobStack.size > 0

    fun cancelActiveJob(stateEventName: String) {
        val job = _activeJobStack.get(stateEventName)
        if (job == null) {
            mahdiLog(TAG, "cancelActiveJob: Job: $stateEventName is null")
            return
        }
        job.cancel()
    }

    fun cancelAllActiveJobs() {
        for ((k, v) in _activeJobStack) {
            cancelActiveJob(k)
        }
    }

    fun increaseLoading(jobName: String) {
        //TODO DELETE THIS JOB ONE IT'S NOT EFFISHENT
        _activeJobStack.put(jobName, Job())
    }

    fun decreaseLoading(jobName: String) {
        //TODO DELETE THIS JOB ONE IT'S NOT EFFISHENT
        _activeJobStack.remove(jobName)

    }

    fun getCurrentViewStateOrNew(): ViewState {
        return viewState.value ?: initNewViewState()
    }

    fun setViewState(viewState: ViewState) {
        _viewState.value = viewState
    }

    fun clearStateMessage(index: Int = 0) {
        _messageStack.clearStateMessage(index)
    }

    abstract suspend fun getResultByStateEvent(stateEvent: OneShotOperationsStateEvent): DataState<ViewState>

    abstract fun initNewViewState(): ViewState

    abstract fun handleNewData(viewState: ViewState)

    override fun onCleared() {
        _activeJobStack.clear()
        mahdiLog(TAG, "onCleared called")
        //TODO dont let here to cancel insert
        super.onCleared()
    }

    private fun now() = System.currentTimeMillis()

}