package com.ssmmhh.jibam.presentation.transactions

import android.content.SharedPreferences
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.Month
import com.ssmmhh.jibam.data.model.SummaryMoney
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.data.source.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.data.util.*
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.presentation.common.MonthManger
import com.ssmmhh.jibam.presentation.transactions.state.TransactionsStateEvent
import com.ssmmhh.jibam.presentation.transactions.state.TransactionsViewState
import com.ssmmhh.jibam.presentation.transactions.state.TransactionsViewState.*
import com.ssmmhh.jibam.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject


@FlowPreview
@ExperimentalCoroutinesApi
class TransactionsViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val monthManger: MonthManger,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel<TransactionsViewState, TransactionsStateEvent>() {

    // Two-way databinding, exposing MutableLiveData
    val searchQuery = MutableLiveData<String>("")

    val isClearSearchQueryButtonVisible: LiveData<Boolean> =
        searchQuery.map { !(it.isNullOrEmpty()) }

    private val _searchViewState: MutableLiveData<SearchViewState> =
        MutableLiveData(SearchViewState.INVISIBLE)
    val searchViewState: LiveData<SearchViewState> = _searchViewState.distinctUntilChanged()

    private val _navigateToAddTransactionEvent = MutableLiveData<Event<Unit>>()
    val navigateToAddTransactionEvent: LiveData<Event<Unit>> = _navigateToAddTransactionEvent

    //Contains the transaction that user deleted by swiping, used for snack bar 'undo' action.
    private var deletedTransactionItem: TransactionDto? = null

    //this is used just to refresh resource if calender type changed in setting
    //actual value of this flow does not matter
    private var _calendarType = MutableStateFlow("")


    private val _transactions: LiveData<List<TransactionsRecyclerViewItem>> =
        combine(
            searchQuery.asFlow().debounce(SEARCH_ACTION_DEBOUNCE_TIME),
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
                return@map addHeaderToTransactions(it)
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
                    stateMessage = getUndoSnackBarStateMessageForDeleteTransaction(),
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
            insertedTransactionRawId = newViewState.insertedTransactionRawId
                ?: outDate.insertedTransactionRawId,
            successfullyDeletedTransactionIndicator = newViewState.successfullyDeletedTransactionIndicator
                ?: outDate.successfullyDeletedTransactionIndicator,
            currentMonth = newViewState.currentMonth ?: outDate.currentMonth,
            calendarType = newViewState.calendarType ?: outDate.calendarType,
        )
    }

    private fun getUndoSnackBarStateMessageForDeleteTransaction(): StateMessage {
        val undoCallback = object : UndoCallback {
            override fun undo() {

                deletedTransactionItem?.let {
                    //Insert deleted transaction
                    insertDeletedTransaction(it)
                } ?: run {
                    //Show error
                    addToMessageStack(
                        message = intArrayOf(R.string.unable_to_restore_transaction),
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    )
                }
            }

            override fun onDismiss() {
                //If you set [deletedTransactionItem] to null here there gone be bug when deleting
                //multiple item fast.
            }
        }

        return StateMessage(
            response = Response(
                intArrayOf(R.string.transaction_successfully_deleted),
                UIComponentType.UndoSnackBar(undoCallback),
                MessageType.Info
            )
        )
    }

    private fun insertDeletedTransaction(deletedTransaction: TransactionDto) {
        launchNewJob(
            TransactionsStateEvent.InsertTransaction(
                deletedTransaction.toTransactionEntity()
            )
        )
        deletedTransactionItem = null
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

    fun openAddTransactionFragment() {
        _navigateToAddTransactionEvent.value = Event(Unit)
    }

    fun clearSearchQuery() {
        searchQuery.value = ""
    }

    fun enableSearchState() {
        _searchViewState.value = SearchViewState.VISIBLE
    }

    fun disableSearchState() {
        _searchViewState.value = SearchViewState.INVISIBLE
    }

    fun isSearchVisible(): Boolean = _searchViewState.value == SearchViewState.VISIBLE

    fun isSearchInvisible(): Boolean = _searchViewState.value == SearchViewState.INVISIBLE

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

    fun deleteTransaction(
        deletedTransaction: TransactionDto,
    ) {
        //add to recently deleted
        deletedTransactionItem = deletedTransaction

        //delete from database
        launchNewJob(
            TransactionsStateEvent.DeleteTransaction(
                transactionId = deletedTransaction.id,
                showSuccessToast = false
            )
        )
    }


    companion object {
        private const val TAG = "TransactionsViewModel"
        private const val GET_TRANSACTION_LIST = "GET_TRANSACTION_LIST"
        private const val GET_SUM_OF_EXPENSES = "GET_SUM_OF_EXPENSES"
        private const val GET_SUM_OF_INCOME = "GET_SUM_OF_INCOME"
        private const val SEARCH_ACTION_DEBOUNCE_TIME: Long = 250
    }
}