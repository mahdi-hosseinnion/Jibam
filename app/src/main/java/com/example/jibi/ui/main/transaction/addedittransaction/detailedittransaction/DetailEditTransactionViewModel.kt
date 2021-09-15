package com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction

import android.util.Log
import com.example.jibi.models.Category
import com.example.jibi.models.Transaction
import com.example.jibi.repository.cateogry.CategoryRepository
import com.example.jibi.repository.tranasction.TransactionRepository
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionPresenterState
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.SubmitButtonState
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionPresenterState
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
import com.example.jibi.ui.main.transaction.common.BaseViewModel
import com.example.jibi.util.Constants
import com.example.jibi.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class DetailEditTransactionViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val currentLocale: Locale
) : BaseViewModel<DetailEditTransactionViewState, DetailEditTransactionStateEvent>() {


    init {
        //retrieve all of categories from cache
        getAllOfCategories()
        //clear state when navigate
        setPresenterState(DetailEditTransactionPresenterState.NoneState)
    }

    override fun initNewViewState(): DetailEditTransactionViewState =
        DetailEditTransactionViewState()

    override suspend fun getResultByStateEvent(
        stateEvent: DetailEditTransactionStateEvent
    ): DataState<DetailEditTransactionViewState> = when (stateEvent) {
        is DetailEditTransactionStateEvent.GetTransactionById -> transactionRepository.getTransactionById(
            stateEvent
        )
        is DetailEditTransactionStateEvent.GetAllOfCategories -> categoryRepository.getAllOfCategories(
            stateEvent
        )
        is DetailEditTransactionStateEvent.DeleteTransaction -> transactionRepository.deleteTransaction(
            stateEvent
        )
    }

    override fun updateViewState(newViewState: DetailEditTransactionViewState): DetailEditTransactionViewState {
        val outdated = getCurrentViewStateOrNew()

        val transaction = newViewState.transaction ?: outdated.transaction

        val transactionCategoryType: Int? =
            newViewState.transactionCategoryType ?: outdated.transactionCategoryType
            ?: transaction?.let {
                if (it.money > 0) {
                    Constants.INCOME_TYPE_MARKER
                } else {
                    Constants.EXPENSES_TYPE_MARKER
                }
            }

        return DetailEditTransactionViewState(
            transaction = transaction,
            transactionCategoryType = transactionCategoryType,
            combineCalender = newViewState.combineCalender ?: outdated.combineCalender,
            allOfCategories = newViewState.allOfCategories ?: outdated.allOfCategories,
            presenterState = newViewState.presenterState ?: outdated.presenterState,
            successfullyDeletedTransactionIndicator = newViewState.successfullyDeletedTransactionIndicator
                ?: outdated.successfullyDeletedTransactionIndicator,
        )
    }

    fun getTransactionById(transactionId: Int) {
        launchNewJob(
            DetailEditTransactionStateEvent.GetTransactionById(transactionId)
        )
    }

    private fun getAllOfCategories() {
        launchNewJob(
            DetailEditTransactionStateEvent.GetAllOfCategories
        )
    }

    fun setPresenterState(newState: DetailEditTransactionPresenterState) {
        setViewState(
            DetailEditTransactionViewState(
                presenterState = newState
            )
        )
    }

    fun getTransactionCategoryId(): Int? = viewState.value?.transaction?.categoryId

    fun getTransaction(): Transaction? = viewState.value?.transaction

    fun getCombineCalender(): GregorianCalendar {
        val viewStateValue = viewState.value?.combineCalender

        if (viewStateValue != null) {
            return viewStateValue
        }
        val transaction = getTransaction()

        val newCalender = GregorianCalendar(currentLocale)

        if (transaction != null) {
            newCalender.timeInMillis = ((transaction.date.toLong()).times(1_000))
        } else {
            Log.e(TAG, "getCombineCalender: combineCalender and transaction is null")
        }

        return newCalender

    }

    fun setToCombineCalender(field: Int, value: Int) {
        val updated = getCombineCalender()
        updated.set(field, value)
        setViewState(
            DetailEditTransactionViewState(
                combineCalender = updated
            )
        )

    }

    fun setToCombineCalender(year: Int, month: Int, day: Int) {
        val updated = getCombineCalender()
        updated.set(year, month, day)
        setViewState(
            DetailEditTransactionViewState(
                combineCalender = updated
            )
        )
    }

    fun setTransactionCategory(category: Category) {
        val transaction = getTransaction()
        setViewState(
            DetailEditTransactionViewState(
                transaction = transaction?.copy(
                    categoryId = category.id,
                    categoryImage = category.img_res,
                    categoryName = category.name
                ),
                transactionCategoryType = category.type
            )
        )
    }


    companion object {
        private const val TAG = "DetailEditTransactionVi"
    }

}
