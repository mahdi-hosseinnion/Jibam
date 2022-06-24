package com.ssmmhh.jibam.presentation.transactiondetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.ssmmhh.jibam.data.model.Transaction
import com.ssmmhh.jibam.data.source.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.presentation.transactiondetail.state.TransactionDetailStateEvent
import com.ssmmhh.jibam.presentation.transactiondetail.state.TransactionDetailViewState
import com.ssmmhh.jibam.util.Event
import javax.inject.Inject

class TransactionDetailViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository
) : BaseViewModel<TransactionDetailViewState, TransactionDetailStateEvent>() {

    private val _transactionId = MutableLiveData<Int>()

    private val _navigateToEditTransactionEvent = MutableLiveData<Event<Unit>>()
    val navigateToEditTransactionEvent: LiveData<Event<Unit>> = _navigateToEditTransactionEvent

    val transaction: LiveData<Transaction?> = _transactionId.switchMap {
        return@switchMap transactionRepository.observeTransaction(it)
            .handleLoadingAndException("observeTransaction")
            .asLiveData()
    }

    override suspend fun getResultByStateEvent(stateEvent: TransactionDetailStateEvent): DataState<TransactionDetailViewState> {
        TODO("Not yet implemented")
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
}