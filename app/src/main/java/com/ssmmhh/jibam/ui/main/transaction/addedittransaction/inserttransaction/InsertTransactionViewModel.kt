package com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction

import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.repository.cateogry.CategoryRepository
import com.ssmmhh.jibam.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionPresenterState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.common.BaseViewModel
import com.ssmmhh.jibam.util.DataState
import com.ssmmhh.jibam.util.Event
import com.ssmmhh.jibam.util.Response
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
        //set calender to current date and time
        setCombineCalender(GregorianCalendar(currentLocale))
        //retrieve all of categories from cache
        getAllOfCategories()
        //when user navigate to this fragment the first thing he se should be category bottomSheet
        setPresenterState(InsertTransactionPresenterState.SelectingCategoryState)
    }


    override fun initNewViewState(): InsertTransactionViewState = InsertTransactionViewState()

    override suspend fun getResultByStateEvent(
        stateEvent: InsertTransactionStateEvent
    ): DataState<InsertTransactionViewState> =
        when (stateEvent) {
            is InsertTransactionStateEvent.InsertTransaction -> {
                val result =  transactionRepository.insertTransaction(
                    stateEvent
                )
                DataState(
                    stateMessage = result.stateMessage,
                    data = InsertTransactionViewState(insertedTransactionRawId = result.data),
                    stateEvent = result.stateEvent
                )
            }

            is InsertTransactionStateEvent.GetAllOfCategories -> {
                val result = categoryRepository.getAllOfCategories(
                    stateEvent
                )
                DataState(
                    stateMessage = result.stateMessage,
                    data = InsertTransactionViewState(allOfCategories = result.data),
                    stateEvent = result.stateEvent
                )
            }

        }

    override fun updateViewState(newViewState: InsertTransactionViewState): InsertTransactionViewState {
        val outDated = getCurrentViewStateOrNew()
        return InsertTransactionViewState(
            categoryEntity = newViewState.categoryEntity
                ?: outDated.categoryEntity,

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

    private fun getAllOfCategories() {
        launchNewJob(
            InsertTransactionStateEvent.GetAllOfCategories
        )
    }

    fun setPresenterState(newState: InsertTransactionPresenterState) {
        setViewState(
            InsertTransactionViewState(
                presenterState = Event(newState)
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
                categoryEntity = item
            )
        )
    }

    fun getTransactionCategory(): Category? = getCurrentViewStateOrNew().categoryEntity

    fun insertTransaction(entity: TransactionEntity) {
        launchNewJob(
            InsertTransactionStateEvent.InsertTransaction(
                transactionEntity = entity
            )
        )
    }
}
