package com.example.jibi.repository.main

import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.persistence.*
import com.example.jibi.repository.buildResponse
import com.example.jibi.repository.safeCacheCall
import com.example.jibi.ui.main.transaction.TransactionListAdapter
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent.*
import com.example.jibi.ui.main.transaction.state.TransactionViewState
import com.example.jibi.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
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

    fun getSumOfIncome(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<Int?> = recordsDao.getSumOfIncome(minDate, maxDate)


    fun getSumOfExpenses(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<Int?> = recordsDao.getSumOfExpenses(minDate, maxDate)


//    recordsDao.getSumOfExpenses(minDate, maxDate)

    //queries
    @FlowPreview
    fun getTransactionList(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<List<Record>?> =
        recordsDao.getRecords(minDate, maxDate).map { currentList ->
            if (currentList.size<1){
                return@map null
            }
            val resultList = ArrayList<Record>()
            var headerDate = currentDateInString(currentList[0].date)
            var incomeSum = 0
            var expensesSum = 0
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
                    incomeSum = 0
                    expensesSum = 0
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
            return@map resultList
        }

    private fun createHeader(date: String, income: Int, expenses: Int, lenght: Int): Record =
        Record(
            id = TransactionListAdapter.HEADER_ITEM,
            money = expenses,
            memo = date,
            cat_id = income,
            date = lenght
        )

    private fun currentDateInString(time: Int): String {
        val dv: Long = ((time.toLong()) * 1000) // its need to be in milisecond
        val df: Date = Date(dv)
        return SimpleDateFormat("MM dd, yyyy", currentLocale).format(df)
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

    /*
    categories transactions
     */
    //queries
    fun getCategoryList(
    ): Flow<List<Category>?> = categoriesDao.getCategories()

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