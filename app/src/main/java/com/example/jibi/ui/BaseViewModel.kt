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

abstract class BaseViewModel<StateEvent, ViewState> : ViewModel() {

    val TAG: String = "AppDebug"

    //    protected val _stateEvent: MutableSharedFlow<StateEvent> = MutableSharedFlow()
    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData()
    protected val _messageStack = MessageStack()
    protected val _activeJobStack = ActiveJobStack()

    private val handler = CoroutineExceptionHandler { _, throwable ->
        addToMessageStack(throwable = throwable)
    }


    val viewState: LiveData<ViewState>
        get() = _viewState

    val stateMessage: LiveData<StateMessage?>
        get() = _messageStack.stateMessage

    //    val loading: LiveData<Boolean> = liveData { TODO("OR")
    val countOfActiveJobs: LiveData<Int>
        get() = _activeJobStack.countOfActiveJobs


    fun launchNewJob(stateEvent: StateEvent) {
        if (_activeJobStack.containsKey(stateEvent.toString())) {
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
        _activeJobStack.put(stateEvent.toString(), job)

        job.invokeOnCompletion { throwable ->
            _activeJobStack.remove(stateEvent.toString())
            if (throwable == null) {
                Log.d(TAG, "launchNewJob: Job: ${stateEvent.toString()} completed normally")
                return@invokeOnCompletion
            }
            if (throwable is CancellationException) {
                Log.d(TAG, "launchNewJob: Job: ${stateEvent.toString()} cancelled normally")
                return@invokeOnCompletion
            }
            addToMessageStack(throwable = throwable)
        }
    }

    private fun addToMessageStack(
        message: String? = null,
        throwable: Throwable? = null,
        uiComponentType: UIComponentType = UIComponentType.Toast,
        messageType: MessageType = MessageType.Error
    ) {
        if (message == null && throwable == null) {
            return
        }
        var message = message
        if (message == null && throwable != null) {
            message = throwable.message
        }
        Log.e(TAG, "launchNewJob: invoke on completion error: $message ", throwable)
        _messageStack.add(
            StateMessage(
                Response(
                    message = message,
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
        val job = _activeJobStack[stateEventName]
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

    fun getCurrentViewStateOrNew(): ViewState {
        val value = viewState.value?.let {
            it
        } ?: initNewViewState()
        return value
    }

    fun setViewState(viewState: ViewState) {
        _viewState.value = viewState
    }

    abstract suspend fun getResultByStateEvent(stateEvent: StateEvent): DataState<ViewState>

    abstract fun initNewViewState(): ViewState

    abstract fun handleNewData(viewState: ViewState)

    override fun onCleared() {
        _activeJobStack.clear()
        super.onCleared()
    }
}