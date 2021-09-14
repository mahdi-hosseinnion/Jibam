package com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction

import com.example.jibi.models.Category
import com.example.jibi.repository.cateogry.CategoryRepository
import com.example.jibi.repository.tranasction.TransactionRepository
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionPresenterState
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
import com.example.jibi.ui.main.transaction.common.BaseViewModel
import com.example.jibi.util.DataState
import java.util.*
import javax.inject.Inject

class InsertTransactionViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val currentLocale: Locale
) : BaseViewModel<InsertTransactionViewState, InsertTransactionStateEvent>() {

    init {
        setPresenterState(InsertTransactionPresenterState.SelectingCategoryState)
    }

    override fun initNewViewState(): InsertTransactionViewState = InsertTransactionViewState()

    override suspend fun getResultByStateEvent(
        stateEvent: InsertTransactionStateEvent
    ): DataState<InsertTransactionViewState> =
        when (stateEvent) {
            is InsertTransactionStateEvent.InsertTransaction -> transactionRepository.insertTransaction(
                stateEvent
            )
            is InsertTransactionStateEvent.GetAllOfCategories -> categoryRepository.getAllOfCategories(
                stateEvent
            )
        }

    override fun updateViewState(newViewState: InsertTransactionViewState): InsertTransactionViewState {
        val outDated = getCurrentViewStateOrNew()
        return InsertTransactionViewState(
            category = newViewState.category
                ?: outDated.category,

            moneyStr = newViewState.moneyStr
                ?: outDated.moneyStr,

            finalMoney = newViewState.finalMoney
                ?: outDated.finalMoney,

            memo = newViewState.memo
                ?: outDated.memo,

            combineCalender = newViewState.combineCalender
                ?: outDated.combineCalender,

            allOfCategories = newViewState.allOfCategories
                ?: outDated.allOfCategories,

            insertedTransactionRawId = newViewState.insertedTransactionRawId
                ?: outDated.insertedTransactionRawId,

            newViewState.presenterState
                ?: outDated.presenterState
        )
    }

    fun setPresenterState(newState: InsertTransactionPresenterState) {
        setViewState(
            InsertTransactionViewState(
                presenterState = newState
            )
        )
    }

    fun setToCombineCalender(field: Int, value: Int) {
        val updated = getCombineCalender()
        updated.set(field, value)
        setViewState(
            InsertTransactionViewState(
                combineCalender = updated
            )
        )

    }

    fun setToCombineCalender(year: Int, month: Int, day: Int) {
        val updated = getCombineCalender()
        updated.set(year, month, day)
        setViewState(
            InsertTransactionViewState(
                combineCalender = updated
            )
        )
    }

    fun setCombineCalender(calender: GregorianCalendar) {
        setViewState(
            InsertTransactionViewState(
                combineCalender = calender
            )
        )
    }

    fun getCombineCalender(): GregorianCalendar {
        val viewStateValue = getCurrentViewStateOrNew().combineCalender
        if (viewStateValue != null) {
            return viewStateValue
        }
        val new = GregorianCalendar(currentLocale)
        setCombineCalender(new)
        return new
    }

    fun setTransactionCategory(item: Category) {
        setViewState(
            InsertTransactionViewState(
                category = item
            )
        )
    }
}