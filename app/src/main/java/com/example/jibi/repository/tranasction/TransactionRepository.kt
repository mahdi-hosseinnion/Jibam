package com.example.jibi.repository.tranasction

import com.example.jibi.models.PieChartData
import com.example.jibi.models.Transaction
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionViewState
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsStateEvent
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsViewState
import com.example.jibi.util.DataState
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getTransactionList(
        minDate: Int? = null,
        maxDate: Int? = null,
        query: String = ""
    ): Flow<List<Transaction>>

    fun getSumOfIncome(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<Double?>


    fun getSumOfExpenses(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<Double?>

    fun getPieChartData(
        minDate: Int,
        maxDate: Int
    ): Flow<List<PieChartData>>

    fun getAllTransactionByCategoryId(
        categoryId: Int,
        minDate: Int,
        maxDate: Int
    ): Flow<List<Transaction>>

    suspend fun insertTransaction(
        stateEvent: TransactionsStateEvent.InsertTransaction
    ): DataState<TransactionsViewState>

    suspend fun deleteTransaction(
        stateEvent: TransactionsStateEvent.DeleteTransaction
    ): DataState<TransactionsViewState>

    suspend fun insertTransaction(
        stateEvent: AddEditTransactionStateEvent.InsertTransaction
    ): DataState<AddEditTransactionViewState>

    suspend fun insertTransaction(
        stateEvent: InsertTransactionStateEvent.InsertTransaction
    ): DataState<InsertTransactionViewState>

    suspend fun deleteTransaction(
        stateEvent: AddEditTransactionStateEvent.DeleteTransaction
    ): DataState<AddEditTransactionViewState>

    suspend fun getTransactionById(
        stateEvent: AddEditTransactionStateEvent.GetTransactionById
    ): DataState<AddEditTransactionViewState>


}