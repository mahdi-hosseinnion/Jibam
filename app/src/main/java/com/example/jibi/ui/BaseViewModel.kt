package com.example.jibi.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jibi.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

abstract class BaseViewModel<OneShotOperationsStateEvent,ViewState> : ViewModel() {

    val TAG: String = "AppDebug"

    //    protected val _stateEvent: MutableSharedFlow<StateEvent> = MutableSharedFlow()
    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData()
    protected val _messageStack = MessageStack()
    protected val _activeJobStack = ActiveJobStack()

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


    fun launchNewJob(stateEvent: OneShotOperationsStateEvent) {
        if (stateEvent !is StateEvent){
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
            handleNewDataState(dataState)
        }
        //add job to active job stack
        _activeJobStack.put(stateEvent.getId(), job)

        job.invokeOnCompletion { throwable ->
            _activeJobStack.remove(stateEvent.getId())
            if (throwable == null) {
                Log.d(TAG, "launchNewJob: Job: ${stateEvent.getId()} completed normally")
                return@invokeOnCompletion
            }
            if (throwable is CancellationException) {
                Log.d(TAG, "launchNewJob: Job: ${stateEvent.getId()} cancelled normally")
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
        Log.e(TAG, "launchNewJob: invoke on completion error: $mMessage ", throwable)
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

    fun cancelActiveJob(stateEventName: String) {
        val job = _activeJobStack.get(stateEventName)
        if (job == null) {
            Log.d(TAG, "cancelActiveJob: Job: $stateEventName is null")
            return
        }
        job.cancel()
    }

    fun cancelAllActiveJobs() {
        for ((k, v) in _activeJobStack) {
            cancelActiveJob(k)
        }
    }

    fun increaseLoading(jobName: String){
        //TODO DELETE THIS JOB ONE IT'S NOT EFFISHENT
        _activeJobStack.put(jobName, Job())
    }
    fun decreaseLoading(jobName:String){
        //TODO DELETE THIS JOB ONE IT'S NOT EFFISHENT
        _activeJobStack.remove(jobName)

    }

    fun getCurrentViewStateOrNew(): ViewState {
        return viewState.value ?: initNewViewState()
    }

    fun setViewState(viewState: ViewState) {
        _viewState.value = viewState
    }

    abstract suspend fun getResultByStateEvent(stateEvent: OneShotOperationsStateEvent): DataState<ViewState>

    abstract fun initNewViewState(): ViewState

    abstract fun handleNewData(viewState: ViewState)

    override fun onCleared() {
        _activeJobStack.clear()
        super.onCleared()
    }
}