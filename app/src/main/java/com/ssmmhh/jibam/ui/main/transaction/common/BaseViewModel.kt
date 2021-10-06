package com.ssmmhh.jibam.ui.main.transaction.common

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssmmhh.jibam.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

abstract class BaseViewModel<_ViewState, _StateEvent : StateEvent>() : ViewModel() {

    private val _viewState: MutableLiveData<_ViewState> = MutableLiveData()
    val viewState: LiveData<_ViewState>
        get() = _viewState

    private val _messageStack = MessageStack()

    private val _activeJobStack = ActiveJobStack()

    fun getAllActiveJobs(): List<String> = _activeJobStack

    val stateMessage: LiveData<StateMessage?>
        get() = _messageStack.stateMessage

    val countOfActiveJobs: LiveData<Int>
        get() = _activeJobStack.countOfActiveJobs

    private val handler = CoroutineExceptionHandler { _, throwable ->
        //should we remove job from _activeJobStack?
        addToMessageStack(throwable = throwable)
    }

    fun launchNewJob(stateEvent: _StateEvent) {

        if (_activeJobStack.contains(stateEvent.getId())) {
            //if already job is active
            return
        }

        val job = viewModelScope.launch(Dispatchers.IO + handler) {

            ensureActive()
            val dataState = getResultByStateEvent(stateEvent)

            ensureActive()
            handleNewDataState(dataState)
        }
        //add job to active job stack
        _activeJobStack.add(stateEvent.getId())

        job.invokeOnCompletion { throwable ->
            _activeJobStack.remove(stateEvent.getId())
            //handle nonCancelable jobs

            if (throwable == null) {
                mahdiLog(TAG, "launchNewJob: Job: completed normally  ${stateEvent.getId()}")
                return@invokeOnCompletion
            }
            if (throwable is CancellationException) {
                mahdiLog(
                    TAG,
                    "launchNewJob: Job: cancelled normally  ${stateEvent.getId()} msg: ${throwable.message} cuze: ${throwable.cause?.message} , $throwable , ${throwable.cause}"
                )
                return@invokeOnCompletion
            }
            addToMessageStack(throwable = throwable)
        }
    }

    private suspend fun handleNewDataState(dataState: DataState<_ViewState>) {
        withContext(Dispatchers.Main) {
            ensureActive()
            //        handleStateEvent(stateEvent).onEach{ dataState -> TODO("OR")
            dataState.data?.let { viewState ->
//                        it.getContentIfNotHandled()?.let { viewState ->TODO ("OR")
                ensureActive()
                setViewState(viewState)
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

    fun <T> Flow<T>.handleLoadingAndException(eventName: String): Flow<T> =
        this
            .distinctUntilChanged()
            .onStart {
                _activeJobStack.add(eventName)
            }.catch { cause ->
                addToMessageStack(throwable = cause)
            }
            .map {
                _activeJobStack.remove(eventName)
                return@map it
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

    fun areAnyJobsActive(): Boolean = _activeJobStack.size > 0

    fun clearStateMessage(index: Int = 0) {
        _messageStack.clearStateMessage(index)
    }

    fun addToMessageStack(
        stateMessage: StateMessage
    ) {
        Log.d(TAG, "addToMessageStack: new stateMessage: ${stateMessage}")
        _messageStack.add(
            stateMessage
        )

    }

    fun getCurrentViewStateOrNew(): _ViewState {
        return  _viewState.value ?:  initNewViewState()
    }

    fun setViewState(viewState: _ViewState) {
        //TODO FIX THIS PRBLE WITH getCurrentViewStateOrNew
        //when we try to set data to viewState with launch(Main) app will lag if we try to call  getCurrentViewStateOrNew
//        viewModelScope.launch(Main) {
            //other viewState data should maintain
            _viewState.value = updateViewState(viewState)
//        }
    }

    abstract fun initNewViewState(): _ViewState

    abstract suspend fun getResultByStateEvent(stateEvent: _StateEvent): DataState<_ViewState>

    abstract fun updateViewState(newViewState: _ViewState): _ViewState

}