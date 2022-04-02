package com.ssmmhh.jibam.presentation.transactions

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ssmmhh.jibam.data.model.Month
import com.ssmmhh.jibam.data.model.SummaryMoney
import com.ssmmhh.jibam.data.model.TransactionsRecyclerViewItem
import com.ssmmhh.jibam.data.source.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.presentation.common.MonthManger
import com.ssmmhh.jibam.presentation.transactions.state.TransactionsStateEvent
import com.ssmmhh.jibam.presentation.transactions.state.TransactionsViewState
import com.ssmmhh.jibam.presentation.transactions.state.TransactionsViewState.*
import com.ssmmhh.jibam.util.isCalendarSolar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@FlowPreview
@ExperimentalCoroutinesApi
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


    private val _transactions: LiveData<List<TransactionsRecyclerViewItem>> =
        combine(
            _queryChannel,
            monthManger.currentMonth,
            _calendarType
        ) { query, month, _ ->
            return@combine TransactionsQueryRequirement(query, month)
        }.flatMapLatest {
            getTransactionList(it.month.startOfMonth, it.month.endOfMonth, it.query)
        }.asLiveData()

    val transactions: LiveData<List<TransactionsRecyclerViewItem>> = _transactions

    private fun getTransactionList(
        fromDate: Long,
        toDate: Long,
        query: String
    ): Flow<List<TransactionsRecyclerViewItem>> =
        transactionRepository.getTransactionList(
            fromDate = fromDate,
            toDate = toDate,
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
        fromDate: Long,
        toDate: Long
    ): Flow<SummaryMoney> = combine(
        transactionRepository.observeSumOfIncomesBetweenDates(fromDate, toDate)
            .handleLoadingAndException(GET_SUM_OF_INCOME),
        transactionRepository.observeSumOfExpensesBetweenDates(fromDate, toDate)
            .handleLoadingAndException(GET_SUM_OF_EXPENSES)
    ) { income, expenses ->
        return@combine SummaryMoney(
            expenses = expenses,
            income = income
        )
    }

    fun setSearchQuery(query: String) {
        viewModelScope.launch {
            _queryChannel.emit(query)
        }
    }

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
            is TransactionsStateEvent.InsertTransaction -> {
                val result = transactionRepository.insertTransaction(stateEvent)
                DataState(
                    stateMessage = result.stateMessage,
                    data = TransactionsViewState(
                        insertedTransactionRawId = result.data
                    ),
                    stateEvent = result.stateEvent
                )
            }

            is TransactionsStateEvent.DeleteTransaction -> {
                val result = transactionRepository.deleteTransaction(stateEvent)
                DataState(
                    stateMessage = result.stateMessage,
                    data = TransactionsViewState(
                        successfullyDeletedTransactionIndicator = result.data
                    ),
                    stateEvent = result.stateEvent
                )
            }
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