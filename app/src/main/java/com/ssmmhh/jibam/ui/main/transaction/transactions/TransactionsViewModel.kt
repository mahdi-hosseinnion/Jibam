package com.ssmmhh.jibam.ui.main.transaction.transactions

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ssmmhh.jibam.models.Month
import com.ssmmhh.jibam.models.Transaction
import com.ssmmhh.jibam.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.ui.main.transaction.common.BaseViewModel
import com.ssmmhh.jibam.ui.main.transaction.common.MonthManger
import com.ssmmhh.jibam.ui.main.transaction.transactions.state.TransactionsStateEvent
import com.ssmmhh.jibam.ui.main.transaction.transactions.state.TransactionsViewState
import com.ssmmhh.jibam.ui.main.transaction.transactions.state.TransactionsViewState.*
import com.ssmmhh.jibam.util.DataState
import com.ssmmhh.jibam.util.isCalendarSolar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@FlowPreview
@ExperimentalCoroutinesApi
//@MainScope
class TransactionsViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val monthManger: MonthManger,
    private val resources: Resources,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel<TransactionsViewState, TransactionsStateEvent>() {
    init {
        setSearchViewState(SearchViewState.INVISIBLE)
    }

    //search stuff
    // In our ViewModel
    private var _queryChannel = MutableStateFlow("")

    //this is used just to refresh resource if calender type changed in setting
    //actual value of this flow does not matter
    private var _calendarType = MutableStateFlow("")


    private val _transactions: LiveData<List<Transaction>> =
        combine(
            _queryChannel,
            monthManger.currentMonth,
            _calendarType
        ) { query, month, _ ->
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
                return@map AddHeaderToTransactions(
                    currentLocale,
                    resources,
                    sharedPreferences.isCalendarSolar(currentLocale)
                ).addHeaderToTransactions(
                    it
                )
            }

    private val _summeryMoney: LiveData<SummaryMoney> =
        monthManger.currentMonth.flatMapLatest {
            setCurrentMonth(it)
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
                recentlyDeletedFields = recentlyDeletedHeader
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
            recentlyDeletedFields = newViewState.recentlyDeletedFields
                ?: outDate.recentlyDeletedFields,
            insertedTransactionRawId = newViewState.insertedTransactionRawId
                ?: outDate.insertedTransactionRawId,
            successfullyDeletedTransactionIndicator = newViewState.successfullyDeletedTransactionIndicator
                ?: outDate.successfullyDeletedTransactionIndicator,
            searchViewState = newViewState.searchViewState ?: outDate.searchViewState,
            currentMonth = newViewState.currentMonth ?: outDate.currentMonth,
            calendarType = newViewState.calendarType ?: outDate.calendarType,
        )
    }

    fun getCalenderType(): String? = viewState.value?.calendarType

    fun setCalenderType(newValue: String?) {
        setViewState(
            TransactionsViewState(
                calendarType = newValue
            )
        )
    }

    fun calenderTypeHaveBeenChangedTo(value: String) {
        setCalenderType(value)
        _calendarType.value = value
        monthManger.refreshData()
    }

    fun setSearchViewState(searchViewState: SearchViewState) {
        setViewState(
            TransactionsViewState(searchViewState = searchViewState)
        )
    }

    fun isSearchVisible(): Boolean =
        getCurrentViewStateOrNew().searchViewState == SearchViewState.VISIBLE

    fun isSearchInVisible(): Boolean =
        getCurrentViewStateOrNew().searchViewState == SearchViewState.INVISIBLE

    private fun setCurrentMonth(month: Month) {
        setViewState(
            TransactionsViewState(currentMonth = month)
        )
    }

    fun showMonthPickerBottomSheet(parentFragmentManager: FragmentManager) {
        monthManger.showMonthPickerBottomSheet(parentFragmentManager)
    }

    fun navigateToPreviousMonth() {
        monthManger.navigateToPreviousMonth()
    }

    fun navigateToNextMonth() {
        monthManger.navigateToNextMonth()
    }


    companion object {
        private const val GET_TRANSACTION_LIST = "GET_TRANSACTION_LIST"
        private const val GET_SUM_OF_EXPENSES = "GET_SUM_OF_EXPENSES"
        private const val GET_SUM_OF_INCOME = "GET_SUM_OF_INCOME"
    }
}