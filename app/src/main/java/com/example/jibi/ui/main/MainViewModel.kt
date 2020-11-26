package com.example.jibi.ui.main

import com.example.jibi.di.main.MainScope
import com.example.jibi.repository.buildResponse
import com.example.jibi.repository.main.MainRepository
import com.example.jibi.ui.BaseViewModel
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent.*
import com.example.jibi.ui.main.transaction.state.TransactionViewState
import com.example.jibi.util.DataState
import com.example.jibi.util.UIComponentType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@MainScope
class MainViewModel
@Inject
constructor(
    private val mainRepository: MainRepository
) : BaseViewModel<OneShotOperationsTransactionStateEvent, TransactionViewState>() {

    override suspend fun getResultByStateEvent(stateEvent: OneShotOperationsTransactionStateEvent): DataState<TransactionViewState> {
        return when (stateEvent) {
            is InsertTransaction ->
                mainRepository.insertTransaction(
                    stateEvent.record, stateEvent
                )

            is GetSpecificTransaction -> mainRepository.getTransaction(
                stateEvent.transactionId, stateEvent
            )

            is UpdateTransaction -> mainRepository.updateTransaction(
                stateEvent.record, stateEvent
            )

            is DeleteTransaction -> mainRepository.deleteTransaction(
                stateEvent.record, stateEvent
            )


            else -> {
                DataState.error(
                    buildResponse(
                        message = "UNKNOWN STATE EVENT!",
                        uiComponentType = UIComponentType.Toast
                    )
                )
            }

        }
    }

    override fun initNewViewState(): TransactionViewState = TransactionViewState()

    override fun handleNewData(viewState: TransactionViewState) {
        viewState.summeryMoney?.let {
            val update = getCurrentViewStateOrNew()
                .copy(summeryMoney = it)
            setViewState(update)
        }
        viewState.transactionList?.let {
            val update = getCurrentViewStateOrNew()
                .copy(transactionList = it)
            setViewState(update)
        }
    }
}
