package com.ssmmhh.jibam.repository.tranasction

import com.ssmmhh.jibam.models.ChartData
import com.ssmmhh.jibam.persistence.dtos.TransactionDto
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.chart.state.ChartStateEvent
import com.ssmmhh.jibam.ui.main.transaction.chart.state.ChartViewState
import com.ssmmhh.jibam.ui.main.transaction.common.state.DeleteTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.common.state.InsertNewTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.transactions.state.TransactionsStateEvent
import com.ssmmhh.jibam.ui.main.transaction.transactions.state.TransactionsViewState
import com.ssmmhh.jibam.util.DataState
import com.ssmmhh.jibam.util.StateEvent
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface TransactionRepository {

    fun getTransactionList(
        fromDate: Long,
        toDate: Long,
        query: String = ""
    ): Flow<List<TransactionDto>>

    fun observeSumOfExpensesBetweenDates(
        fromDate: Long,
        toDate: Long
    ): Flow<BigDecimal>

    fun observeSumOfIncomesBetweenDates(
        fromDate: Long,
        toDate: Long
    ): Flow<BigDecimal>


    fun getPieChartData(
        fromDate: Long,
        toDate: Long
    ): Flow<List<ChartData>>

    fun getAllTransactionByCategoryId(
        categoryId: Int,
        fromDate: Long,
        toDate: Long
    ): Flow<List<TransactionDto>>

    suspend fun deleteTransaction(
        stateEvent:DeleteTransactionStateEvent
    ): DataState<Int>

    suspend fun insertTransaction(
        stateEvent: InsertNewTransactionStateEvent
    ): DataState<Long>

    suspend fun updateTransaction(
        stateEvent: DetailEditTransactionStateEvent.UpdateTransaction
    ): DataState<DetailEditTransactionViewState>

    suspend fun getTransactionById(
        stateEvent: DetailEditTransactionStateEvent.GetTransactionById
    ): DataState<DetailEditTransactionViewState>


}