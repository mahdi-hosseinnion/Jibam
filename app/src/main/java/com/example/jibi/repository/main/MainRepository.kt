package com.example.jibi.repository.main

import com.example.jibi.di.main.MainScope
import com.example.jibi.persistence.CategoriesDao
import com.example.jibi.persistence.RecordsDao
import com.example.jibi.repository.JobManager
import com.example.jibi.ui.main.transaction.state.TransactionViewState
import com.example.jibi.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map


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


}