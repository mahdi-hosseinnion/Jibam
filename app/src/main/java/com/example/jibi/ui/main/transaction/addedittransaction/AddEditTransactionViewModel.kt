package com.example.jibi.ui.main.transaction.addedittransaction

import com.example.jibi.di.main.MainScope
import com.example.jibi.repository.tranasction.TransactionRepository
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionViewState
import com.example.jibi.ui.main.transaction.common.NewBaseViewModel
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsViewState
import com.example.jibi.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class AddEditTransactionViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository
) : NewBaseViewModel<AddEditTransactionViewState, AddEditTransactionStateEvent>() {


    override fun initNewViewState(): AddEditTransactionViewState = AddEditTransactionViewState()

    override suspend fun getResultByStateEvent(stateEvent: AddEditTransactionStateEvent): DataState<AddEditTransactionViewState> =
        when (stateEvent) {
            is AddEditTransactionStateEvent.InsertTransaction -> transactionRepository.insertTransaction(
                stateEvent
            )
            is AddEditTransactionStateEvent.DeleteTransaction -> transactionRepository.deleteTransaction(
                stateEvent
            )
            is AddEditTransactionStateEvent.GetTransactionById -> transactionRepository.getTransactionById(
                stateEvent
            )
        }

    override fun updateViewState(newViewState: AddEditTransactionViewState): AddEditTransactionViewState {
        val outDate = getCurrentViewStateOrNew()
        return AddEditTransactionViewState(
            transaction = newViewState.transaction ?: outDate.transaction
        )
    }
}