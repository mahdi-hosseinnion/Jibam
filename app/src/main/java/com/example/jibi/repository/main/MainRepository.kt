package com.example.jibi.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.SummaryMoney
import com.example.jibi.persistence.CategoriesDao
import com.example.jibi.persistence.RecordsDao
import com.example.jibi.repository.JobManager
import com.example.jibi.ui.main.transaction.state.TransactionViewState
import com.example.jibi.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@MainScope
class MainRepository
constructor(
    val recordsDao: RecordsDao,
    val categoriesDao: CategoriesDao
) : JobManager("MainRepository") {

    fun getTransactions(
        fromDate: Int,
        toDate: Int
    ): Flow<DataState<TransactionViewState>> =
//    ): Flow<DataState<TransactionViewState>> = flow {
//        //TODO(add limit time for fromData and to data and check if is -1 or not )
//        emit(recordsDao.getRecords())
//    }
        recordsDao.getAllRecords().map { recordList ->
            if (recordList.isEmpty()) {
                DataState.error<TransactionViewState>(
                    response = Response(
                        "Error getting the list of transactions nothing is in database",
                        ResponseType.Toast()
                    )
                )
            } else {
                DataState.data(
                    data = TransactionViewState(
                        transactionList = recordList
                    ),
                )
            }
        }


    fun getSummaryMoney(): Flow<TransactionViewState> = flow {
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
    }

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