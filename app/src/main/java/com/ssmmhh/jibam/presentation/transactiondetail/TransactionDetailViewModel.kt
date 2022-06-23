package com.ssmmhh.jibam.presentation.transactiondetail

import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.presentation.transactiondetail.state.TransactionDetailStateEvent
import com.ssmmhh.jibam.presentation.transactiondetail.state.TransactionDetailViewState
import javax.inject.Inject

class TransactionDetailViewModel
@Inject
constructor(

) : BaseViewModel<TransactionDetailViewState, TransactionDetailStateEvent>() {


    override suspend fun getResultByStateEvent(stateEvent: TransactionDetailStateEvent): DataState<TransactionDetailViewState> {
        TODO("Not yet implemented")
    }

    override fun updateViewState(newViewState: TransactionDetailViewState): TransactionDetailViewState =
        TransactionDetailViewState()

    override fun initNewViewState(): TransactionDetailViewState = TransactionDetailViewState()
}