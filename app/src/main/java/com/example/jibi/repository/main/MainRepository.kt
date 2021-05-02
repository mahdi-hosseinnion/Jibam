package com.example.jibi.repository.main

import android.util.Log
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.models.SearchModel
import com.example.jibi.persistence.*
import com.example.jibi.repository.buildResponse
import com.example.jibi.repository.safeCacheCall
import com.example.jibi.ui.main.transaction.TransactionListAdapter
import com.example.jibi.ui.main.transaction.TransactionListAdapter.Companion.TODAY
import com.example.jibi.ui.main.transaction.TransactionListAdapter.Companion.YESTERDAY
import com.example.jibi.ui.main.transaction.ViewCategoriesFragment
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent.*
import com.example.jibi.ui.main.transaction.state.TransactionViewState
import com.example.jibi.util.*
import com.example.jibi.util.Constants.Companion.CACHE_TIMEOUT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.exp

@ExperimentalCoroutinesApi
@MainScope
class MainRepository
@Inject
constructor(
    val recordsDao: RecordsDao,
    val categoriesDao: CategoriesDao,
    val currentLocale: Locale
) {
    var solarCalendar = SolarCalendar()
    var today: String? = null
    var yesterday: String? = null

    fun getSumOfIncome(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<Double?> = recordsDao.getSumOfIncome(minDate, maxDate)


    fun getSumOfExpenses(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<Double?> = recordsDao.getSumOfExpenses(minDate, maxDate)


//    recordsDao.getSumOfExpenses(minDate, maxDate)

    //queries
    @FlowPreview
    fun getTransactionList(
        minDate: Int? = null,
        maxDate: Int? = null,
        searchModel: SearchModel
    ): Flow<List<Record>?> =
        recordsDao.getRecords(minDate, maxDate, searchModel).map { currentList ->
            if (currentList.isEmpty()) {
                return@map null
            }
            val resultList = ArrayList<Record>()
            var headerDate = currentDateInString(currentList[0].date)
            var incomeSum = 0.0
            var expensesSum = 0.0
            var tempList = ArrayList<Record>()
            for (item in currentList) {
                if (currentDateInString(item.date) == headerDate) {
                    //make new header and items
                    tempList.add(item)
                    if (item.money >= 0) { //income
                        incomeSum += item.money
                    } else { //expenses
                        expensesSum += item.money
                    }
                } else {
                    //add header and items
                    resultList.add(createHeader(headerDate, incomeSum, expensesSum, tempList.size))
                    resultList.addAll(tempList)
                    //clear item to defualt
                    headerDate = currentDateInString(item.date)
                    tempList.clear()
                    incomeSum = 0.0
                    expensesSum = 0.0
                    //make new header and items
                    tempList.add(item)
                    if (item.money >= 0) { //income
                        incomeSum += item.money
                    } else { //expenses
                        expensesSum += item.money
                    }
                }
            }
            //add header and items
            resultList.add(createHeader(headerDate, incomeSum, expensesSum, tempList.size))
            resultList.addAll(tempList)
            //clear item to defualt
            today = null
            yesterday = null
            return@map resultList
        }

    private fun createHeader(date: String, income: Double, expenses: Double, lenght: Int): Record {
        return if (lenght > 1) {
            Record(
                id = TransactionListAdapter.HEADER_ITEM,
                money = expenses,
                incomeSum = income,
                memo = date,
                cat_id = 0,
                date = 0
            )
        } else {
            Record(
                id = TransactionListAdapter.HEADER_ITEM,
                money = 0.0,
                memo = date,
                cat_id = 0,
                date = 0
            )
        }
    }

    private fun currentDateInString(time: Int): String {
        val dv: Long = ((time.toLong()) * 1000) // its need to be in milisecond

        val transDate =getFormattedDate(dv)

        if (today == null) {
            today = getFormattedDate(System.currentTimeMillis())
        }
        if (yesterday == null) {
            yesterday = getFormattedDate(System.currentTimeMillis().minus(86_400_000L))
        }
        if (transDate == today) {
            return TODAY
        }
        if (transDate == yesterday) {
            return YESTERDAY
        }
        return transDate
    }

    private fun getFormattedDate(unixTimeStamp: Long): String {
        val df: Date = Date(unixTimeStamp)

        return if (currentLocale.country=="IR") {
            solarCalendar.calcSolarCalendar(df, currentLocale)
        } else {
            SimpleDateFormat(
                TransactionListAdapter.HEADER_DATE_PATTERN,
                currentLocale
            ).format(df)
        }

    }

    //dataBase main dao
    suspend fun insertTransaction(
        stateEvent: InsertTransaction
    ): DataState<TransactionViewState> {
        mahdiLog(TAG, "insertTransaction 001 called${stateEvent.record} ")
        val cacheResult = safeCacheCall {
            recordsDao.insertOrReplace(stateEvent.record)
        }

        return object : CacheResponseHandler<TransactionViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<TransactionViewState> {
                mahdiLog(TAG, "insertTransaction 001 HAVE BEEN DONE called${stateEvent.record} ")
                tryIncreaseCategoryOrdering(stateEvent.record.cat_id)
                return DataState.data(
                    response = buildResponse(
                        message = "Transaction Successfully inserted",
                        UIComponentType.Toast,
                        MessageType.Success
                    )
                )
            }
        }.getResult()
    }

    private suspend fun tryIncreaseCategoryOrdering(categoryId: Int) {
        try {
            withTimeout(CACHE_TIMEOUT.times(2)) {
                val category =
                    categoriesDao.getCategoryById(categoryId)
                if (category == null ||
                    category.ordering < 0 || //if category got pinned
                    category.ordering > Int.MAX_VALUE.minus(2)//if its to big even bigger then int max value
                ) {
                    return@withTimeout
                }
                //increase category ordering
                categoriesDao.updateCategory(category.copy(ordering = category.ordering.plus(1)))
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "tryIncreaseCategoryOrdering: unable to update category order with id:$categoryId",
                e
            )
        }
    }

//        safeCacheCall { recordsDao.insertOrReplace(record) }

    suspend fun getTransaction(
        stateEvent: GetSpecificTransaction
    ): DataState<TransactionViewState> {
        val cacheResult = safeCacheCall {
            recordsDao.getRecordById(stateEvent.transactionId)
        }

        return object : CacheResponseHandler<TransactionViewState, Record>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Record): DataState<TransactionViewState> {
                return DataState.data(
                    response = buildResponse(
                        message = "Transaction Successfully returned",
                        UIComponentType.None,
                        MessageType.Success
                    )
                )
            }
        }.getResult()
    }

    suspend fun updateTransaction(
        stateEvent: UpdateTransaction
    ): DataState<TransactionViewState> {
        val cacheResult = safeCacheCall {
            recordsDao.updateRecord(stateEvent.record)
        }
        return object : CacheResponseHandler<TransactionViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<TransactionViewState> {
                return DataState.data(
                    response = buildResponse(
                        message = "Transaction Successfully Updated",
                        UIComponentType.Toast,
                        MessageType.Success
                    )
                )
            }
        }.getResult()
    }

    suspend fun deleteTransaction(
        stateEvent: DeleteTransaction
    ): DataState<TransactionViewState> {
        val cacheResult = safeCacheCall {
            recordsDao.deleteRecord(stateEvent.record)
        }
        return object : CacheResponseHandler<TransactionViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<TransactionViewState> {
                return DataState.data(
                    response = buildResponse(
                        message = "Transaction Successfully Deleted",
                        UIComponentType.Toast,
                        MessageType.Success
                    )
                )
            }
        }.getResult()
    }

    suspend fun deleteCategory(
        stateEvent: DeleteCategory
    ): DataState<TransactionViewState> {
        val cacheResult = safeCacheCall {
            categoriesDao.deleteCategory(category = stateEvent.category)
        }
        return object : CacheResponseHandler<TransactionViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<TransactionViewState> {
                return DataState.data(
                    response = buildResponse(
                        message = "Category Successfully Deleted",
                        UIComponentType.Toast,
                        MessageType.Success
                    )
                )
            }
        }.getResult()
    }

    suspend fun insertCategory(
        stateEvent: InsertCategory
    ): DataState<TransactionViewState> {
        val cacheResult = safeCacheCall {
            categoriesDao.insertOrReplace(category = stateEvent.category)
        }
        return object : CacheResponseHandler<TransactionViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<TransactionViewState> {
                return DataState.data(
                    response = buildResponse(
                        message = "Category Successfully Deleted",
                        UIComponentType.Toast,
                        MessageType.Success
                    )
                )
            }
        }.getResult()
    }

    @FlowPreview
    //TODO ADD SOME REORDER IF NEEDED
    suspend fun pinOrUnpinCategory(
        stateEvent: PinOrUnpinCategory
    ): DataState<TransactionViewState> {
        //TODO SYNC THE CATEGORIES ORDERING
        val lastCategory = stateEvent.category
        val newCategory =
            if (lastCategory.ordering < 0) {//if pinned before so unpin
                var newOrder = try {
                    categoriesDao.getMaxOfOrdering().plus(1)
                } catch (e: Exception) {
                    1
                }
                if (newOrder < 1) {
                    newOrder = 1
                }
                lastCategory.copy(ordering = newOrder)
            } else { //if not pinned so pin this category
                var newOrder = try {
                    categoriesDao.getMinOfOrdering().minus(1)
                } catch (e: Exception) {
                    -1
                }
                if (newOrder > -1) {
                    newOrder = -1
                }
                lastCategory.copy(ordering = newOrder)
            }

        val cacheResult = safeCacheCall {
            categoriesDao.updateCategory(category = newCategory)
        }

        return object : CacheResponseHandler<TransactionViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<TransactionViewState> {
                return DataState.data(
                    response = buildResponse(
                        message = "Category Successfully Deleted",
                        UIComponentType.Toast,
                        MessageType.Success
                    )
                )
            }
        }.getResult()
    }

    suspend fun updateCategory(
        stateEvent: UpdateCategory
    ): DataState<TransactionViewState> {
        val cacheResult = safeCacheCall {
            categoriesDao.updateCategory(stateEvent.category)
        }
        return object : CacheResponseHandler<TransactionViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<TransactionViewState> {
                return DataState.data(
                    response = buildResponse(
                        message = "Transaction Successfully Updated",
                        UIComponentType.None,
                        MessageType.Success
                    )
                )
            }
        }.getResult()
    }


    /*
    categories transactions
     */
//queries
    fun getCategoryList(
    ): Flow<List<Category>?> = categoriesDao.getCategories()

    fun getCategoryImages() = categoriesDao.getCategoriesImages()
/*    fun getSummaryMoney(): Flow<TransactionViewState> = flow {
        val result = MutableLiveData<TransactionViewState>()
        result.value = TransactionViewState(summeryMoney = SummaryMoney())
        recordsDao.returnTheSumOfAllIncome().collect { income ->
            result.value = result.value.apply {
                this?.summeryMoney?.copy(
                    income = income
                )
            }
        }
        recordsDao.returnTheSumOfAllExpenses().collect { expenses ->
            result.value = result.value.apply {
                this?.summeryMoney?.copy(
                    expenses = expenses
                )
            }
        }
        emitAll(result.asFlow())
    }.onEach { transactionViewState ->
        transactionViewState.summeryMoney?.apply {
            this.balance = (this.income + this.expenses)
        }
    }*/

/*    try {
        coroutineScope {
            val mayFailAsync1 = async {
                mayFail1()
            }
            val mayFailAsync2 = async {
                mayFail2()
            }
            useResult(mayFailAsync1.await(), mayFailAsync2.await())
        }
    } catch (e: IOException) {
        // handle this
        throw MyIoException("Error doing IO", e)
    } catch (e: AnotherException) {
        // handle this too
        throw MyOtherException("Error doing something", e)
    }*/

}