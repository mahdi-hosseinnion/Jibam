package com.ssmmhh.jibam.data.source.repository.tranasction

import android.content.res.Resources
import androidx.annotation.StringRes
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.ChartData
import com.ssmmhh.jibam.data.source.local.dao.TransactionDao
import com.ssmmhh.jibam.data.source.local.dto.ChartDataDto
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity
import com.ssmmhh.jibam.data.source.repository.buildResponse
import com.ssmmhh.jibam.data.source.repository.safeCacheCall
import com.ssmmhh.jibam.data.util.*
import com.ssmmhh.jibam.presentation.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.ssmmhh.jibam.presentation.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.ssmmhh.jibam.presentation.common.state.DeleteTransactionStateEvent
import com.ssmmhh.jibam.presentation.common.state.InsertNewTransactionStateEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject

class TransactionRepositoryImpl
@Inject
constructor(
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    override fun getTransactionList(
        fromDate: Long,
        toDate: Long,
        query: String
    ): Flow<List<TransactionDto>> = transactionDao.observeAllOfTransactionsBetweenDates(
        fromDate = fromDate,
        toDate = toDate,
        query = query
    )

    override fun observeSumOfExpensesBetweenDates(fromDate: Long, toDate: Long): Flow<BigDecimal> =
        transactionDao.observeSumOfExpensesBetweenDates(fromDate, toDate)

    override fun observeSumOfIncomesBetweenDates(fromDate: Long, toDate: Long): Flow<BigDecimal> =
        transactionDao.observeSumOfIncomesBetweenDates(fromDate, toDate)

    override fun getPieChartData(fromDate: Long, toDate: Long): Flow<List<ChartData>> =
        transactionDao.getSumOfEachCategoryMoney(fromDate = fromDate, toDate = toDate)
            .map { calculatePercentage(it) }

    private fun calculatePercentage(values: List<ChartDataDto>): List<ChartData> {
        //calculate the sum of all of moneys for further percentage calculation
        val sumOfAllExpenses = values.filter { it.isExpensesCategory }.sumOf { it.sumOfMoney }
        val sumOfAllIncome = values.filter { it.isIncomeCategory }.sumOf { it.sumOfMoney }

        /**
         * calculate percentage of each data
         * map it to ChartData
         */
        return values.map { item ->
            val divisionScale = 3
            val percentage = when (item.categoryType) {
                CategoryEntity.EXPENSES_TYPE_MARKER -> {
                    //change scale to 3 then times it to 100 for single decimal result
                    (item.sumOfMoney.setScale(divisionScale)
                        .div(sumOfAllExpenses)).times(BigDecimal("100"))
                }
                CategoryEntity.INCOME_TYPE_MARKER -> {
                    (item.sumOfMoney.setScale(divisionScale)
                        .div(sumOfAllIncome)).times(BigDecimal("100"))
                }
                else -> {
                    BigDecimal("-1")
                }
            }
            return@map item.toChartData(percentage.toFloat())
        }

    }


    override fun getAllTransactionByCategoryId(
        categoryId: Int,
        fromDate: Long,
        toDate: Long
    ): Flow<List<TransactionDto>> = transactionDao.observeAllOfTransactionsWithCategoryId(
        categoryId = categoryId,
        fromDate = fromDate,
        toDate = toDate
    )

    override suspend fun insertTransaction(
        stateEvent: InsertNewTransactionStateEvent
    ): DataState<Long> {

        val cacheResult = safeCacheCall {
            transactionDao.insertTransaction(
                stateEvent.transaction.toTransactionDto().toTransactionEntity()
            )
        }

        return object : CacheResponseHandler<Long, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<Long> {
                return if (resultObj > 0) {
                    DataState.data(
                        response = buildResponse(
                            message = intArrayOf(R.string.transaction_successfully_inserted),
                            UIComponentType.Toast,
                            MessageType.Success
                        ),
                        data = resultObj
                    )
                } else {
                    DataState.error(
                        response = buildResponse(
                            message = intArrayOf(R.string.transaction_error_inserted),
                            UIComponentType.Toast,
                            MessageType.Success
                        ),
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()
    }

    override suspend fun updateTransaction(
        stateEvent: DetailEditTransactionStateEvent.UpdateTransaction
    ): DataState<DetailEditTransactionViewState> {

        val cacheResult = safeCacheCall {
            transactionDao.updateTransaction(transactionEntity = stateEvent.transactionEntity)
        }
        return object : CacheResponseHandler<DetailEditTransactionViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<DetailEditTransactionViewState> {
                return if (resultObj > 0) {
                    DataState.data(
                        response = buildResponse(
                            message = intArrayOf(R.string.transaction_successfully_updated),
                            UIComponentType.Toast,
                            MessageType.Success
                        ),
                        data = null
                    )
                } else {
                    DataState.error(
                        response = buildResponse(
                            message = intArrayOf(R.string.transaction_error_updated),
                            UIComponentType.Toast,
                            MessageType.Success
                        ),
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()
    }

    override suspend fun deleteTransaction(
        stateEvent: DeleteTransactionStateEvent
    ): DataState<Int> {
        val cacheResult = safeCacheCall {
            transactionDao.deleteTransaction(stateEvent.transactionId)
        }
        return object : CacheResponseHandler<Int, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<Int> {
                return if (resultObj > 0) {
                    //success
                    val uiComponentType: UIComponentType = if (stateEvent.showSuccessToast) {
                        UIComponentType.Toast
                    } else {
                        UIComponentType.None
                    }
                    DataState.data(
                        response = Response(
                            message = intArrayOf(R.string.transaction_successfully_deleted),
                            uiComponentType = uiComponentType,
                            messageType = MessageType.Success
                        ),
                        data = resultObj,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.error(
                        response = Response(
                            message = intArrayOf(R.string.transaction_error_deleted),
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        )
                    )
                }

            }
        }.getResult()
    }


    override suspend fun getTransactionById(
        stateEvent: DetailEditTransactionStateEvent.GetTransactionById
    ): DataState<DetailEditTransactionViewState> {
        val cacheResult = safeCacheCall {
            transactionDao.getTransactionById(stateEvent.transactionId)
        }

        return object : CacheResponseHandler<DetailEditTransactionViewState, TransactionDto>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: TransactionDto): DataState<DetailEditTransactionViewState> {
                return DataState.data(
                    response = buildResponse(
                        message = null,
                        UIComponentType.None,
                        MessageType.Success
                    ),
                    data = DetailEditTransactionViewState(
                        defaultTransaction = resultObj
                    )
                )
            }
        }.getResult()
    }


}