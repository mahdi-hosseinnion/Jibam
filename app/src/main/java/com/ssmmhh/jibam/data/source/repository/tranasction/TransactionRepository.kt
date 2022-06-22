package com.ssmmhh.jibam.data.source.repository.tranasction

import com.ssmmhh.jibam.data.model.ChartData
import com.ssmmhh.jibam.data.model.Transaction
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.presentation.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.ssmmhh.jibam.presentation.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.ssmmhh.jibam.presentation.common.state.DeleteTransactionStateEvent
import com.ssmmhh.jibam.presentation.common.state.InsertNewTransactionStateEvent
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.presentation.addedittransaction.state.AddEditTransactionStateEvent
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
        stateEvent: AddEditTransactionStateEvent.GetTransactionById
    ): DataState<Transaction>


}