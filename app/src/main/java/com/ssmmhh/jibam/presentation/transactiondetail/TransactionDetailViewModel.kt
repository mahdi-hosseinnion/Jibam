package com.ssmmhh.jibam.presentation.transactiondetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.Transaction
import com.ssmmhh.jibam.data.source.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.data.util.AreYouSureCallback
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.data.util.MessageType
import com.ssmmhh.jibam.data.util.UIComponentType
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.presentation.transactiondetail.state.TransactionDetailStateEvent
import com.ssmmhh.jibam.presentation.transactiondetail.state.TransactionDetailViewState
import com.ssmmhh.jibam.util.Event
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TransactionDetailViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository
) : BaseViewModel<TransactionDetailViewState, TransactionDetailStateEvent>() {

    private val _transactionId = MutableLiveData<Int>()

    private val _navigateToEditTransactionEvent = MutableLiveData<Event<Unit>>()
    val navigateToEditTransactionEvent: LiveData<Event<Unit>> = _navigateToEditTransactionEvent

    private val _navigateBackEvent = MutableLiveData<Event<Unit>>()
    val navigateBackEvent: LiveData<Event<Unit>> = _navigateBackEvent

    val transaction: LiveData<Transaction?> = _transactionId.switchMap {
        return@switchMap transactionRepository.observeTransaction(it)
            .handleLoadingAndException("observeTransaction")
            .asLiveData()
    }

    override suspend fun getResultByStateEvent(stateEvent: TransactionDetailStateEvent): DataState<TransactionDetailViewState> =
        when (stateEvent) {
            is TransactionDetailStateEvent.DeleteTransaction -> {
                val result = transactionRepository.deleteTransaction(
                    stateEvent
                )
                withContext(Main) {
                    if (result.stateMessage?.response?.messageType == MessageType.Success) {
                        _navigateBackEvent.value = Event(Unit)
                    }
                }
                DataState(
                    data = null,
                    stateEvent = result.stateEvent,
                    stateMessage = result.stateMessage
                )
            }
        }


    override fun updateViewState(newViewState: TransactionDetailViewState): TransactionDetailViewState =
        TransactionDetailViewState()

    override fun initNewViewState(): TransactionDetailViewState = TransactionDetailViewState()

    fun start(transactionId: Int) {
        _transactionId.value = transactionId
    }

    fun openEditTransaction() {
        _navigateToEditTransactionEvent.value = Event(Unit)
    }

    fun showAreYouSureDialogBeforeDelete() {
        val callback = object : AreYouSureCallback {
            override fun proceed() {
                deleteTransaction()
            }

            override fun cancel() {}
        }
        addToMessageStack(
            message = intArrayOf(R.string.are_you_sure_delete_transaction),
            uiComponentType = UIComponentType.AreYouSureDialog(callback),
            messageType = MessageType.Info
        )
    }

    private fun deleteTransaction() {
        val id: Int = _transactionId.value ?: return
        launchNewJob(
            stateEvent = TransactionDetailStateEvent.DeleteTransaction(
                transactionId = id
            )
        )
    }
}