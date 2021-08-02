package com.example.jibi.repository.tranasction

import com.example.jibi.models.Transaction
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

    suspend fun insertTransaction(
        stateEvent: TransactionsStateEvent.InsertTransaction
    ): DataState<TransactionsViewState>

    suspend fun deleteTransaction(
        stateEvent: TransactionsStateEvent.DeleteTransaction
    ): DataState<TransactionsViewState>

    suspend fun insertTransaction(
        stateEvent: AddEditTransactionStateEvent.InsertTransaction
    ): DataState<AddEditTransactionViewState>

    suspend fun deleteTransaction(
        stateEvent: AddEditTransactionStateEvent.DeleteTransaction
    ): DataState<AddEditTransactionViewState>

    suspend fun getTransactionById(
        stateEvent: AddEditTransactionStateEvent.GetTransactionById
    ): DataState<AddEditTransactionViewState>
}