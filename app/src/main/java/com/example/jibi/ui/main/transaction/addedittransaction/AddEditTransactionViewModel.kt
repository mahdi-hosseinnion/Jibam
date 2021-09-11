package com.example.jibi.ui.main.transaction.addedittransaction

import com.example.jibi.models.Category
import com.example.jibi.repository.cateogry.CategoryRepository
import com.example.jibi.repository.tranasction.TransactionRepository
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionViewState
import com.example.jibi.ui.main.transaction.addedittransaction.state.PresenterState
import com.example.jibi.ui.main.transaction.common.BaseViewModel
import com.example.jibi.util.Constants.EXPENSES_OTHER_CATEGORY_ID
import com.example.jibi.util.DataState
import com.example.jibi.util.mahdiLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
//@MainScope
class AddEditTransactionViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : BaseViewModel<AddEditTransactionViewState, AddEditTransactionStateEvent>() {
    private val TAG = "AddEditTransactionViewM"

    init {
        launchNewJob(AddEditTransactionStateEvent.GetAllOfCategories)
    }

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
            is AddEditTransactionStateEvent.GetAllOfCategories -> categoryRepository.getAllOfCategories(
                stateEvent
            )
        }

    override fun updateViewState(newViewState: AddEditTransactionViewState): AddEditTransactionViewState {
        val outDate = getCurrentViewStateOrNew()
        return AddEditTransactionViewState(
            transaction = newViewState.transaction ?: outDate.transaction,
            categoriesList = newViewState.categoriesList ?: outDate.categoriesList,
            insertedTransactionRawId = newViewState.insertedTransactionRawId
                ?: outDate.insertedTransactionRawId,
            successfullyDeletedTransactionIndicator = newViewState.successfullyDeletedTransactionIndicator
                ?: outDate.successfullyDeletedTransactionIndicator   ,
            presenterState = newViewState.presenterState
                ?: outDate.presenterState
        )
    }

    fun getCategoriesList(): List<Category> {
        val result = getCurrentViewStateOrNew().categoriesList
        if (result == null) {
            launchNewJob(AddEditTransactionStateEvent.GetAllOfCategories)
            return emptyList()
        }
        return result
    }

    fun getSelectedCategoryId(): Int = getCurrentViewStateOrNew().transaction?.categoryId ?: EXPENSES_OTHER_CATEGORY_ID

    fun setPresenterState(newState: PresenterState) {
        setViewState(
            AddEditTransactionViewState(
                presenterState = newState
            )
        )
    }
}