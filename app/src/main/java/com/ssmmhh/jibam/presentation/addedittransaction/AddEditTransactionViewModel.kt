package com.ssmmhh.jibam.presentation.addedittransaction

import com.ssmmhh.jibam.data.source.repository.cateogry.CategoryRepository
import com.ssmmhh.jibam.data.source.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.presentation.addedittransaction.state.AddEditTransactionStateEvent
import com.ssmmhh.jibam.presentation.addedittransaction.state.AddEditTransactionViewState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import java.util.*
import javax.inject.Inject

class AddEditTransactionViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val currentLocale: Locale
) : BaseViewModel<AddEditTransactionViewState, AddEditTransactionStateEvent>() {

    override suspend fun getResultByStateEvent(stateEvent: AddEditTransactionStateEvent): DataState<AddEditTransactionViewState> {
        return when (stateEvent) {
            else -> DataState.data()
        }
    }

    override fun updateViewState(newViewState: AddEditTransactionViewState): AddEditTransactionViewState =
        AddEditTransactionViewState()

    override fun initNewViewState(): AddEditTransactionViewState = AddEditTransactionViewState()
}