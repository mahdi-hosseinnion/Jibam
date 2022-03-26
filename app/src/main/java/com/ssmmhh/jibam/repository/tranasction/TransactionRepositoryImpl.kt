package com.ssmmhh.jibam.repository.tranasction

import android.content.res.Resources
import androidx.annotation.StringRes
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.models.ChartData
import com.ssmmhh.jibam.persistence.daos.TransactionDao
import com.ssmmhh.jibam.persistence.dtos.ChartDataDto
import com.ssmmhh.jibam.persistence.dtos.TransactionDto
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.repository.buildResponse
import com.ssmmhh.jibam.repository.safeCacheCall
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.chart.state.ChartStateEvent
import com.ssmmhh.jibam.ui.main.transaction.chart.state.ChartViewState
import com.ssmmhh.jibam.ui.main.transaction.transactions.state.TransactionsStateEvent
import com.ssmmhh.jibam.ui.main.transaction.transactions.state.TransactionsViewState
import com.ssmmhh.jibam.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal
import javax.inject.Inject

class TransactionRepositoryImpl
@Inject
constructor(
    private val transactionDao: TransactionDao,
    private val _resources: Resources
) : TransactionRepository {

    override fun getTransactionList(
        fromDate: Long,
        toDate: Long,
        query: String
    ): Flow<List<TransactionDto>> = transactionDao.getAllOfTransactionsBetweenDates(
        fromDate = fromDate,
        toDate = toDate,
        query = query
    )

    override fun observeSumOfExpensesBetweenDates(fromDate: Long, toDate: Long): Flow<BigDecimal> =
        transactionDao.observeSumOfExpensesBetweenDates(fromDate, toDate)

    override fun observeSumOfIncomesBetweenDates(fromDate: Long, toDate: Long): Flow<BigDecimal> =
        transactionDao.observeSumOfIncomesBetweenDates(fromDate, toDate)

    override fun getPieChartData(fromDate: Long, toDate: Long): Flow<List<ChartData>> = flow {
        emit(
            calculatePercentage(
                transactionDao.getSumOfEachCategoryMoney(
                    fromDate = fromDate,
                    toDate = toDate
                )
            )
        )
    }

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
    ): Flow<List<TransactionDto>> = transactionDao.getAllTransactionByCategoryId(
        categoryId = categoryId,
        fromDate = fromDate,
        toDate = toDate
    )


    fun getString(@StringRes id: Int) = _resources.getString(id)

    override suspend fun insertTransaction(
        stateEvent: InsertTransactionStateEvent.InsertTransaction
    ): DataState<InsertTransactionViewState> {

        val cacheResult = safeCacheCall {
            transactionDao.insertTransaction(stateEvent.transactionEntity)
        }

        return object : CacheResponseHandler<InsertTransactionViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<InsertTransactionViewState> {
                return if (resultObj > 0) {
                    DataState.data(
                        response = buildResponse(
                            message = getString(R.string.transaction_successfully_inserted),
                            UIComponentType.Toast,
                            MessageType.Success
                        ),
                        data = InsertTransactionViewState(
                            insertedTransactionRawId = resultObj
                        )
                    )
                } else {
                    DataState.error(
                        response = buildResponse(
                            message = getString(R.string.transaction_error_inserted),
                            UIComponentType.Toast,
                            MessageType.Success
                        ),
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()
    }

    override suspend fun insertTransaction(
        stateEvent: ChartStateEvent.InsertTransaction
    ): DataState<ChartViewState> {

        val cacheResult = safeCacheCall {
            transactionDao.insertTransaction(stateEvent.transactionEntity)
        }

        return object : CacheResponseHandler<ChartViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<ChartViewState> {
                return if (resultObj > 0) {
                    DataState.data(
                        response = buildResponse(
                            message = getString(R.string.transaction_successfully_inserted),
                            UIComponentType.Toast,
                            MessageType.Success
                        ),
                        data = null
                    )
                } else {
                    DataState.error(
                        response = buildResponse(
                            message = getString(R.string.transaction_error_inserted),
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
                            message = getString(R.string.transaction_successfully_updated),
                            UIComponentType.Toast,
                            MessageType.Success
                        ),
                        data = null
                    )
                } else {
                    DataState.error(
                        response = buildResponse(
                            message = getString(R.string.transaction_error_updated),
                            UIComponentType.Toast,
                            MessageType.Success
                        ),
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()
    }

    override suspend fun insertTransaction(
        stateEvent: TransactionsStateEvent.InsertTransaction
    ): DataState<TransactionsViewState> {

        val cacheResult = safeCacheCall {
            transactionDao.insertTransaction(stateEvent.transactionEntity)
        }

        return object : CacheResponseHandler<TransactionsViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<TransactionsViewState> {
                return if (resultObj > 0) {
                    DataState.data(
                        response = buildResponse(
                            message = getString(R.string.transaction_successfully_inserted),
                            UIComponentType.Toast,
                            MessageType.Success
                        ),
                        data = TransactionsViewState(
                            insertedTransactionRawId = resultObj
                        )
                    )
                } else {
                    DataState.error(
                        response = buildResponse(
                            message = getString(R.string.transaction_error_inserted),
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
        stateEvent: TransactionsStateEvent.DeleteTransaction
    ): DataState<TransactionsViewState> {
        val cacheResult = safeCacheCall {
            transactionDao.deleteTransaction(stateEvent.transactionEntity)
        }
        return object : CacheResponseHandler<TransactionsViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<TransactionsViewState> {
                return if (resultObj > 0) {
                    //success
                    val uiComponentType: UIComponentType = if (stateEvent.showSuccessToast) {
                        UIComponentType.Toast
                    } else {
                        UIComponentType.None
                    }
                    DataState.data(
                        response = Response(
                            message = getString(R.string.transaction_successfully_deleted),
                            uiComponentType = uiComponentType,
                            messageType = MessageType.Success
                        ),
                        data = TransactionsViewState(
                            successfullyDeletedTransactionIndicator = resultObj
                        ),
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.error(
                        response = Response(
                            message = getString(R.string.transaction_error_deleted),
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        )
                    )
                }

            }
        }.getResult()
    }

    override suspend fun deleteTransaction(
        stateEvent: DetailEditTransactionStateEvent.DeleteTransaction
    ): DataState<DetailEditTransactionViewState> {
        val cacheResult = safeCacheCall {
            transactionDao.deleteTransaction(stateEvent.transactionId)
        }
        return object : CacheResponseHandler<DetailEditTransactionViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<DetailEditTransactionViewState> {
                return if (resultObj > 0) {
                    //success
                    val uiComponentType: UIComponentType = if (stateEvent.showSuccessToast) {
                        UIComponentType.Toast
                    } else {
                        UIComponentType.None
                    }
                    DataState.data(
                        response = Response(
                            message = getString(R.string.transaction_successfully_deleted),
                            uiComponentType = uiComponentType,
                            messageType = MessageType.Success
                        ),
                        data = DetailEditTransactionViewState(
                            successfullyDeletedTransactionIndicator = resultObj
                        ),
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.error(
                        response = Response(
                            message = getString(R.string.transaction_error_deleted),
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        )
                    )
                }

            }
        }.getResult()
    }

    override suspend fun deleteTransaction(
        stateEvent: ChartStateEvent.DeleteTransaction
    ): DataState<ChartViewState> {
        val cacheResult = safeCacheCall {
            transactionDao.deleteTransaction(stateEvent.transactionId)
        }
        return object : CacheResponseHandler<ChartViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<ChartViewState> {
                return if (resultObj > 0) {
                    //success
                    val uiComponentType: UIComponentType = if (stateEvent.showSuccessToast) {
                        UIComponentType.Toast
                    } else {
                        UIComponentType.None
                    }
                    DataState.data(
                        response = Response(
                            message = getString(R.string.transaction_successfully_deleted),
                            uiComponentType = uiComponentType,
                            messageType = MessageType.Success
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.error(
                        response = Response(
                            message = getString(R.string.transaction_error_deleted),
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
                        message = "Transaction Successfully returned",
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