package com.example.jibi.ui.main.transaction.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Transaction
import com.example.jibi.repository.tranasction.TransactionRepository
import com.example.jibi.ui.main.transaction.MonthManger
import com.example.jibi.ui.main.transaction.common.NewBaseViewModel
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsStateEvent
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsViewState
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsViewState.*
import com.example.jibi.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class TransactionsViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val monthManger: MonthManger,
    private val currentLocale: Locale
) : NewBaseViewModel<TransactionsViewState, TransactionsStateEvent>() {

    //search stuff
    // In our ViewModel
    private var _queryChannel = MutableStateFlow("")


    private val _transactions: LiveData<List<Transaction>> =
        combine(
            _queryChannel,
            monthManger.currentMonth
        ) { query, month ->
            return@combine TransactionsQueryRequirement(query, month)
        }.flatMapLatest {
            getTransactionList(it.month.startOfMonth, it.month.endOfMonth, it.query)
        }.asLiveData()

    val transactions: LiveData<List<Transaction>> = _transactions

    private fun getTransactionList(
        minData: Int,
        maxDate: Int,
        query: String
    ): Flow<List<Transaction>> =
        transactionRepository.getTransactionList(
            minDate = minData,
            maxDate = maxDate,
            query = query
        ).handleLoadingAndException(GET_TRANSACTION_LIST)
            .map {
                return@map AddHeaderToTransactions(currentLocale).addHeaderToTransactions(
                    it
                )
            }

    private val _summeryMoney: LiveData<SummaryMoney> =
        monthManger.currentMonth.flatMapLatest {
            getSummeryMoney(it.startOfMonth, it.endOfMonth)
        }.asLiveData()

    val summeryMoney: LiveData<SummaryMoney> = _summeryMoney

    private fun getSummeryMoney(
        minDate: Int,
        maxDate: Int
    ): Flow<SummaryMoney> = combine(
        transactionRepository.getSumOfIncome(minDate, maxDate)
            .handleLoadingAndException(GET_SUM_OF_INCOME),
        transactionRepository.getSumOfExpenses(minDate, maxDate)
            .handleLoadingAndException(GET_SUM_OF_EXPENSES)
    ) { _income, _expenses ->
        val income: Double = _income ?: 0.0
        val expenses: Double = _expenses ?: 0.0
        return@combine SummaryMoney(
            balance = income.plus(expenses),//expenses is negative
            expenses = expenses,
            income = income
        )
    }

    fun setSearchQuery(query: String) {
        viewModelScope.launch {
            _queryChannel.emit(query)
        }
    }

/*
    fun insertTransaction(newTransaction: Record) = viewModelScope.launch {
        increaseLoading(INSERT_TRANSACTION)

        val dataState = transactionRepository.insertTransaction(newTransaction)
        handleNewDataState(dataState)

        if (dataState.wasSuccessful()) {
            _transactionInsertEvent.value = Event(Unit)
        }

        decreaseLoading(INSERT_TRANSACTION)
    }

    fun deleteTransaction(transaction: Record, showSuccessToast: Boolean) = viewModelScope.launch {
        increaseLoading(DELETE_TRANSACTION)

        val dataState = transactionRepository.deleteTransaction(transaction, showSuccessToast)
        handleNewDataState(dataState)

        if (dataState.wasSuccessful()) {
            _transactionInsertEvent.value = Event(Unit)
        }

        decreaseLoading(DELETE_TRANSACTION)
    }
*/

    fun setRecentlyDeletedTrans(recentlyDeletedHeader: RecentlyDeletedTransaction?) {
        setViewState(
            TransactionsViewState(
                recentlyDeletedFields  = recentlyDeletedHeader
            )
        )
    }

    fun getRecentlyDeletedTrans(): RecentlyDeletedTransaction? =
        getCurrentViewStateOrNew().recentlyDeletedFields

    override fun initNewViewState(): TransactionsViewState = TransactionsViewState()

    override suspend fun getResultByStateEvent(stateEvent: TransactionsStateEvent): DataState<TransactionsViewState> =
        when (stateEvent) {
            is TransactionsStateEvent.InsertTransaction -> transactionRepository
                .insertTransaction(stateEvent)
            is TransactionsStateEvent.DeleteTransaction -> transactionRepository
                .deleteTransaction(stateEvent)
        }

    override fun updateViewState(newViewState: TransactionsViewState): TransactionsViewState {
        val outDate = getCurrentViewStateOrNew()
        return TransactionsViewState(
            recentlyDeletedFields  = newViewState.recentlyDeletedFields ?: outDate.recentlyDeletedFields
        )
    }

    companion object {
        private const val GET_TRANSACTION_LIST = "GET_TRANSACTION_LIST"
        private const val GET_SUM_OF_EXPENSES = "GET_SUM_OF_EXPENSES"
        private const val GET_SUM_OF_INCOME = "GET_SUM_OF_INCOME"
    }
}