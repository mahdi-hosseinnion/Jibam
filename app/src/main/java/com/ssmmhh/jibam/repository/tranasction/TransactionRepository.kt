package com.ssmmhh.jibam.repository.tranasction

import com.ssmmhh.jibam.models.ChartData
import com.ssmmhh.jibam.persistence.dtos.TransactionDto
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.chart.state.ChartStateEvent
import com.ssmmhh.jibam.ui.main.transaction.chart.state.ChartViewState
import com.ssmmhh.jibam.ui.main.transaction.transactions.state.TransactionsStateEvent
import com.ssmmhh.jibam.ui.main.transaction.transactions.state.TransactionsViewState
import com.ssmmhh.jibam.util.DataState
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface TransactionRepository {

    fun getTransactionList(
        minDate: Int? = null,
        maxDate: Int? = null,
        query: String = ""
    ): Flow<List<TransactionDto>>

    fun getSumOfIncome(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<BigDecimal?>


    fun getSumOfExpenses(
        minDate: Int? = null,
        maxDate: Int? = null
    ): Flow<BigDecimal?>

    fun getPieChartData(
        minDate: Int,
        maxDate: Int
    ): Flow<List<ChartData>>

    fun getAllTransactionByCategoryId(
        categoryId: Int,
        minDate: Int,
        maxDate: Int
    ): Flow<List<TransactionDto>>

    suspend fun deleteTransaction(
        stateEvent: TransactionsStateEvent.DeleteTransaction
    ): DataState<TransactionsViewState>

    suspend fun insertTransaction(
        stateEvent: TransactionsStateEvent.InsertTransaction
    ): DataState<TransactionsViewState>

    suspend fun insertTransaction(
        stateEvent: InsertTransactionStateEvent.InsertTransaction
    ): DataState<InsertTransactionViewState>

    suspend fun insertTransaction(
        stateEvent: ChartStateEvent.InsertTransaction
    ): DataState<ChartViewState>

    suspend fun updateTransaction(
        stateEvent: DetailEditTransactionStateEvent.UpdateTransaction
    ): DataState<DetailEditTransactionViewState>

    suspend fun deleteTransaction(
        stateEvent: DetailEditTransactionStateEvent.DeleteTransaction
    ): DataState<DetailEditTransactionViewState>

    suspend fun deleteTransaction(
        stateEvent: ChartStateEvent.DeleteTransaction
    ): DataState<ChartViewState>

    suspend fun getTransactionById(
        stateEvent: DetailEditTransactionStateEvent.GetTransactionById
    ): DataState<DetailEditTransactionViewState>


}