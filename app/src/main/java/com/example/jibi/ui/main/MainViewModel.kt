package com.example.jibi.ui.main

import android.widget.Toast
import com.example.jibi.di.main.MainScope
import com.example.jibi.repository.buildError
import com.example.jibi.repository.buildResponse
import com.example.jibi.repository.main.MainRepository
import com.example.jibi.ui.BaseViewModel
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent.*
import com.example.jibi.ui.main.transaction.state.TransactionViewState
import com.example.jibi.util.DataState
import com.example.jibi.util.StateEvent
import com.example.jibi.util.UIComponentType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@MainScope
class MainViewModel
@Inject
constructor(
    private val mainRepository: MainRepository
) : BaseViewModel<OneShotOperationsTransactionStateEvent, TransactionViewState>() {
    override suspend fun getResultByStateEvent(stateEvent: OneShotOperationsTransactionStateEvent): DataState<TransactionViewState> {
        return when (stateEvent) {
            is InsertTransaction -> {
                mainRepository.insertTransaction(stateEvent.record)
            }
            is GetSpecificTransaction -> mainRepository.getRecordById(stateEvent.transactionId)

            is UpdateTransaction -> mainRepository.updateRecord(stateEvent.record)

            is DeleteTransaction -> mainRepository.deleteRecord(stateEvent.record)


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
