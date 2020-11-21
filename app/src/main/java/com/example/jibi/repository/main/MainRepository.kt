package com.example.jibi.repository.main

import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Record
import com.example.jibi.persistence.*
import com.example.jibi.repository.JobManager
import com.example.jibi.repository.asDataState
import com.example.jibi.repository.safeCacheCall
import com.example.jibi.util.DataState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalCoroutinesApi
@MainScope
class MainRepository
@Inject
constructor(
    val recordsDao: RecordsDao,
    val categoriesDao: CategoriesDao
) : JobManager("MainRepository") {


    fun getSumOfIncome(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<DataState<Int?>> = recordsDao.getSumOfIncome(minDate, maxDate).asDataState()


    fun getSumOfExpenses(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<DataState<Int?>> = recordsDao.getSumOfExpenses(minDate, maxDate).asDataState()


//    recordsDao.getSumOfExpenses(minDate, maxDate)

    //queries
    fun getTransactionList(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<DataState<List<Record>?>> = recordsDao.getRecords(minDate, maxDate).asDataState()


    //dataBase main dao
    fun insertTransaction(record: Record): Flow<DataState<Long>> =
        safeCacheCall(IO, "Insert Transaction") {
            recordsDao.insertOrReplace(record)
        }

    fun getTransaction(transactionId: Int): Flow<DataState<Record>> =
        safeCacheCall(IO, "Get Transaction") {
            recordsDao.getRecordById(transactionId)
        }

    fun updateTransaction(record: Record): Flow<DataState<Int>> =
        safeCacheCall(IO, "Update Transaction") {
            recordsDao.updateRecord(record)
        }

    fun deleteTransaction(record: Record): Flow<DataState<Int>> =
        safeCacheCall(IO, "Delete Transaction") {
            recordsDao.deleteRecord(record)
        }


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