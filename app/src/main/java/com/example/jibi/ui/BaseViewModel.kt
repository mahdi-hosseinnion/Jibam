package com.example.jibi.ui

import android.util.Log
import androidx.lifecycle.*
import com.example.jibi.util.DataState
import com.example.jibi.util.MessageStack
import com.example.jibi.util.StateMessage
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel<StateEvent, ViewState> : ViewModel() {

    val TAG: String = "AppDebug"

    protected val _stateEvent: MutableSharedFlow<StateEvent> = MutableSharedFlow()
    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData()
    protected val _laoding: MutableLiveData<Int> = MutableLiveData()
    protected val _messageStack = MessageStack()


    val viewState: LiveData<ViewState>
        get() = _viewState

    val stateMessage: LiveData<StateMessage?>
        get() = _messageStack.stateMessage

    //    val loading: LiveData<Boolean> = liveData { TODO("OR")
    val loading: LiveData<Int>
        get() = _laoding

    //    val dataState: Flow<DataState<ViewState>>
//    =_stateEvent.collect {
//        handleStateEvent(it)
//    }
    //worked code
/*    val dataState: Flow<DataState<ViewState>> = flow {
        _stateEvent.onEach { stateEvent ->
            emitAll(handleStateEvent(stateEvent))
        }
    }   */
    val dataState: Flow<ViewState> = flow {
        viewModelScope.launch(Main) {
            _stateEvent.collect { stateEvent ->
                handleStateEvent(stateEvent).collect { dataState ->
                    dataState.data?.let { viewState ->
//                        it.getContentIfNotHandled()?.let { viewState ->TODO ("OR")
                        handleNewData(viewState)
                    }
                    dataState.stateMessage.let { stateMessage ->
//                        it.getContentIfNotHandled()?.let { viewState ->TODO ("OR")
                        stateMessage?.let {
                            _messageStack.add(it)
                        }

                    }
                    handleLoading(dataState.isLoading)
                }

            }
        }
    }


    private fun handleLoading(isLoading: Boolean) {
        Log.d(TAG, "handleLoading: isLoading: $isLoading")
        val currentValue: Int = _laoding.value ?: 0
        if (isLoading) {
            _laoding.value = currentValue.plus(1)
        } else {
            if (currentValue > 0) {
                _laoding.value = currentValue.minus(1)
            } else {
                _laoding.value = 0
            }
        }
    }

/*        _stateEvent.collect{ stateEvent ->
            return handleStateEvent(stateEvent = stateEvent)
//            stateEvent?.let {
//                return@map handleStateEvent(stateEvent)
//            } ?: handleStateEvent(stateEvent)
        }
    }*/
    /*Transformations
    .switchMap(_stateEvent) { stateEvent ->
        stateEvent?.let {
            handleStateEvent(stateEvent)
        }
    }*/

    fun setStateEvent(event: StateEvent) {
        viewModelScope.launch {
            _stateEvent.emit(event)
        }
//        _stateEvent.tryEmit(event)
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

    abstract fun handleStateEvent(stateEvent: StateEvent): Flow<DataState<ViewState>>

    abstract fun initNewViewState(): ViewState

    abstract fun handleNewData(viewState: ViewState)
}