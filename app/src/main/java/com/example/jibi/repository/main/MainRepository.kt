package com.example.jibi.repository.main

import androidx.room.Update
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.persistence.*
import com.example.jibi.repository.JobManager
import com.example.jibi.repository.asDataState
import com.example.jibi.repository.buildResponse
import com.example.jibi.repository.safeCacheCall
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent.*
import com.example.jibi.ui.main.transaction.state.TransactionViewState
import com.example.jibi.util.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import java.lang.Exception
import javax.inject.Inject

@ExperimentalCoroutinesApi
@MainScope
class MainRepository
@Inject
constructor(
    val recordsDao: RecordsDao,
    val categoriesDao: CategoriesDao
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
    fun getTransactionList(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<List<Record>?> = recordsDao.getRecords(minDate, maxDate)


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