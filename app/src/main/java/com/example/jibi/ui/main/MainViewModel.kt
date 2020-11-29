package com.example.jibi.ui.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Record
import com.example.jibi.models.SummaryMoney
import com.example.jibi.repository.buildResponse
import com.example.jibi.repository.main.MainRepository
import com.example.jibi.ui.BaseViewModel
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent.*
import com.example.jibi.ui.main.transaction.state.TransactionViewState
import com.example.jibi.util.DataState
import com.example.jibi.util.UIComponentType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@MainScope
class MainViewModel
@Inject
constructor(
    private val mainRepository: MainRepository
) : BaseViewModel<OneShotOperationsTransactionStateEvent, TransactionViewState>() {
    val GET_SUM_OF_ALL_EXPENSES = "getting sum of all expenses"
    val GET_SUM_OF_ALL_INCOME = "getting sum of all income"
    val GET_LIST_OF_TRANSACTION = "getting the list of transaction"

    init {
        Log.d("MainViewModel", "mahdi init called: ")
        //flow stuff
        viewModelScope.launch {
            launch {
                //TODO HANDLE WHERE TO INCREMENT AND WHEN TO DECREMENT LOADING
                mainRepository.getSumOfExpenses()
                    //loading stuff
                    .onStart { increaseLoading(GET_SUM_OF_ALL_EXPENSES) }
                    .catch { cause -> addToMessageStack(throwable = cause) }
                    .collect {
                        it?.let {
                            //loading stuff
                            decreaseLoading(GET_SUM_OF_ALL_EXPENSES)
                            setAllTransactionExpenses(it)
                        }
                    }
            }
            //TODO HANDLE WHERE TO INCREMENT AND WHEN TO DECREMENT LOADING
            launch {
                mainRepository.getSumOfIncome()
                    //loading stuff
                    .onStart { increaseLoading(GET_SUM_OF_ALL_INCOME) }
                    .catch { cause -> addToMessageStack(throwable = cause) }
                    .collect {
                        it?.let {
                            //loading stuff
                            decreaseLoading(GET_SUM_OF_ALL_INCOME)
                            setAllTransactionIncome(it)
                        }
                    }
                //TODO HANDLE WHERE TO INCREMENT AND WHEN TO DECREMENT LOADING
            }
            launch {
                mainRepository.getTransactionList()
                    //loading stuff
                    .onStart { increaseLoading(GET_LIST_OF_TRANSACTION) }
                    .catch { cause -> addToMessageStack(throwable = cause) }
                    .collect {
                        it?.let {
                            //loading stuff
                            decreaseLoading(GET_LIST_OF_TRANSACTION)
                            setListOfTransactions(it)
                        }
                    }
            }
        }
    }


    override suspend fun getResultByStateEvent(stateEvent: OneShotOperationsTransactionStateEvent): DataState<TransactionViewState> {
        return when (stateEvent) {

            is InsertTransaction -> mainRepository.insertTransaction(stateEvent)

            is GetSpecificTransaction -> mainRepository.getTransaction(stateEvent)

            is UpdateTransaction -> mainRepository.updateTransaction(stateEvent)

            is DeleteTransaction -> mainRepository.deleteTransaction(stateEvent)

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

    fun setAllTransactionIncome(newIncome: Int) {
        val update = getCurrentViewStateOrNew()
        if (update.summeryMoney != null) {
            update.summeryMoney = update.summeryMoney?.copy(income = newIncome)
        } else {
            update.summeryMoney = SummaryMoney(income = newIncome)
        }
        setViewState(update)
    }

    fun setAllTransactionExpenses(newExpenses: Int) {
        val update = getCurrentViewStateOrNew()
        if (update.summeryMoney != null) {
            update.summeryMoney = update.summeryMoney?.copy(expenses = newExpenses)
        } else {
            update.summeryMoney = SummaryMoney(expenses = newExpenses)
        }
        setViewState(update)
    }

    fun setListOfTransactions(transactionList: List<Record>) {
        val update = getCurrentViewStateOrNew()
            .copy(transactionList = transactionList)
        setViewState(update)
    }
}
