package com.example.jibi.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.*
import com.example.jibi.repository.main.MainRepository
import com.example.jibi.ui.BaseViewModel
import com.example.jibi.ui.main.transaction.home.TransactionListAdapter.Companion.NO_RESULT_FOUND_FOR_THIS_QUERY_MARKER
import com.example.jibi.ui.main.transaction.home.TransactionListAdapter.Companion.NO_RESULT_FOUND_IN_DATABASE
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent.*
import com.example.jibi.ui.main.transaction.state.TransactionViewState
import com.example.jibi.util.Constants
import com.example.jibi.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

//instance search
//https://blog.mindorks.com/instant-search-using-kotlin-flow-operators
//https://www.hellsoft.se/instant-search-with-kotlin-coroutines/ WE USE THIS
@FlowPreview
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
    val GET_LIST_OF_CATEGORY = "getting the list of category"
    private val TAG1 = "MainViewModel"

    //search stuff
    // In our ViewModel
    var queryChannel = MutableStateFlow(SearchModel())


    init {
        //flow stuff
        viewModelScope.launch {

            launch {
                mainRepository.getCategoryList()
                    //loading stuff
                    .onStart { increaseLoading(GET_LIST_OF_CATEGORY) }
                    .catch { cause -> addToMessageStack(throwable = cause) }
                    .collect {
                        //loading stuff
                        decreaseLoading(GET_LIST_OF_CATEGORY)
                        it?.let {
                            setListOfCategories(it)
                        }
                    }
            }

            launch {
                //TODO HANDLE WHERE TO INCREMENT AND WHEN TO DECREMENT LOADING
                mainRepository.getSumOfExpenses()
                    //loading stuff
                    .onStart { increaseLoading(GET_SUM_OF_ALL_EXPENSES) }
                    .catch { cause -> addToMessageStack(throwable = cause) }
                    .collect {
                        //loading stuff
                        decreaseLoading(GET_SUM_OF_ALL_EXPENSES)
                        it?.let {
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
                        //loading stuff
                        decreaseLoading(GET_SUM_OF_ALL_INCOME)
                        it?.let {
                            setAllTransactionIncome(it)
                        }
                    }
                //TODO HANDLE WHERE TO INCREMENT AND WHEN TO DECREMENT LOADING
            }
            launch {

                queryChannel
                    .debounce(Constants.SEARCH_DEBOUNCE)
                    .distinctUntilChanged()
                    .collectLatest {

                        Log.d(TAG, "searchDEBUG: 2-- ${it.query}")
                        //send query to repository
                        mainRepository.getTransactionList(searchModel = it)
                            //loading stuff
                            .onStart { increaseLoading(GET_LIST_OF_TRANSACTION) }
                            .catch { cause ->
                                addToMessageStack(throwable = cause)
                                Log.d(TAG, "crashed")
                            }
                            .collect { result ->
                                //loading stuff
                                decreaseLoading(GET_LIST_OF_TRANSACTION)
                                if (result != null) {
                                    setListOfTransactions(result)
                                } else {
                                    if (it.isNotEmpty()) {
                                        //contain query params
                                        setListOfTransactions(
                                            listOf(
                                                NO_RESULT_FOUND_FOR_THIS_QUERY_MARKER
                                            )
                                        )
                                    } else {
                                        setListOfTransactions(listOf(NO_RESULT_FOUND_IN_DATABASE))
                                    }
                                }

                            }
                    }
                //special time out for search
                launch {
                    delay(Constants.CACHE_TIMEOUT)
                    decreaseLoading(GET_LIST_OF_TRANSACTION)
                }
            }

            //timeout loading decrese
            launch {
                delay(Constants.CACHE_TIMEOUT)
                decreaseLoading(GET_LIST_OF_CATEGORY)
                decreaseLoading(GET_LIST_OF_TRANSACTION)
                decreaseLoading(GET_SUM_OF_ALL_INCOME)
                decreaseLoading(GET_SUM_OF_ALL_EXPENSES)

            }
        }
    }


    override suspend fun getResultByStateEvent(stateEvent: OneShotOperationsTransactionStateEvent): DataState<TransactionViewState> {
        return when (stateEvent) {

            is InsertTransaction -> mainRepository.insertTransaction(stateEvent)

            is GetSpecificTransaction -> mainRepository.getTransaction(stateEvent)

            is UpdateTransaction -> mainRepository.updateTransaction(stateEvent)

            is DeleteTransaction -> mainRepository.deleteTransaction(stateEvent)

            is DeleteTransactionById -> mainRepository.deleteTransactionById(stateEvent)

            is DeleteCategory -> mainRepository.deleteCategory(stateEvent)

            is InsertCategory -> mainRepository.insertCategory(stateEvent)

            is PinOrUnpinCategory -> mainRepository.pinOrUnpinCategory(stateEvent)

            is UpdateCategory -> mainRepository.updateCategory(stateEvent)

            is GetPieChartData -> mainRepository.getPieChartData(stateEvent)

            is GetAllTransactionByCategoryId -> mainRepository.getAllTransactionByCategoryId(
                stateEvent
            )

            is GetCategoryById -> mainRepository.getCategoryById(stateEvent)
        }
    }

    fun getCategoryImages(): LiveData<List<CategoryImages>> = mainRepository.getCategoryImages()

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
        viewState.pieChartData?.let {
            val update = getCurrentViewStateOrNew()
                .copy(pieChartData = it)
            setViewState(update)
        }
        viewState.detailChartFields.category?.let {
            val current = getCurrentViewStateOrNew()
            val update = current.copy(
                detailChartFields = current.detailChartFields.copy(
                    category = it
                )
            )
            setViewState(update)
        }
        viewState.detailChartFields.allTransaction?.let {
            val current = getCurrentViewStateOrNew()
            val update = current.copy(
                detailChartFields = current.detailChartFields.copy(
                    allTransaction = it
                )
            )
            setViewState(update)

        }
    }

    private fun setAllTransactionIncome(newIncome: Double) {
        val update = getCurrentViewStateOrNew()
        if (update.summeryMoney != null) {
            update.summeryMoney = update.summeryMoney?.copy(income = newIncome)
        } else {
            update.summeryMoney = SummaryMoney(income = newIncome)
        }
        setViewState(update)
    }

    private fun setAllTransactionExpenses(newExpenses: Double) {
        val update = getCurrentViewStateOrNew()
        if (update.summeryMoney != null) {
            update.summeryMoney = update.summeryMoney?.copy(expenses = newExpenses)
        } else {
            update.summeryMoney = SummaryMoney(expenses = newExpenses)
        }
        setViewState(update)
    }

    private fun setListOfTransactions(transactionList: List<Record>) {
        val update = getCurrentViewStateOrNew()
            .copy(transactionList = transactionList)
        setViewState(update)
    }

    private fun setListOfCategories(categoryList: List<Category>) {
        val update = getCurrentViewStateOrNew()
            .copy(categoryList = categoryList)
        setViewState(update)
    }

    fun setDetailTransFields(transaction: Record) {
        val update = getCurrentViewStateOrNew().copy(detailTransFields = transaction)
        setViewState(update)
    }

    fun setRecentlyDeletedTrans(transaction: Record, position: Int, header: Record?) {
        val update = getCurrentViewStateOrNew().copy(
            recentlyDeletedFields = TransactionViewState.RecentlyDeletedFields(
                recentlyDeletedTrans = transaction,
                recentlyDeletedTransPosition = position,
                recentlyDeletedHeader = header
            )
        )
        setViewState(update)
    }

    fun setRecentlyDeletedTransToNull() {
        val update = getCurrentViewStateOrNew().copy(
            recentlyDeletedFields = TransactionViewState.RecentlyDeletedFields()
        )
        setViewState(update)
    }
}
