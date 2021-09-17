package com.example.jibi.repository.tranasction

import android.content.res.Resources
import androidx.annotation.StringRes
import com.example.jibi.R
import com.example.jibi.models.PieChartData
import com.example.jibi.models.Transaction
import com.example.jibi.persistence.RecordsDao
import com.example.jibi.persistence.getRecords
import com.example.jibi.persistence.getSumOfExpenses
import com.example.jibi.persistence.getSumOfIncome
import com.example.jibi.repository.buildResponse
import com.example.jibi.repository.safeCacheCall
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
import com.example.jibi.ui.main.transaction.chart.state.ChartStateEvent
import com.example.jibi.ui.main.transaction.chart.state.ChartViewState
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsStateEvent
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsViewState
import com.example.jibi.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject

class TransactionRepositoryImpl
@Inject
constructor(
    private val recordsDao: RecordsDao,
    private val _resources: Resources
) : TransactionRepository {

    override fun getTransactionList(
        minDate: Int?,
        maxDate: Int?,
        query: String
    ): Flow<List<Transaction>> = recordsDao.getRecords(
        minDate = minDate,
        maxDate = maxDate,
        query = query
    )

    override fun getSumOfIncome(minDate: Int?, maxDate: Int?): Flow<Double?> =
        recordsDao.getSumOfIncome(minDate, maxDate)

    override fun getSumOfExpenses(minDate: Int?, maxDate: Int?): Flow<Double?> =
        recordsDao.getSumOfExpenses(minDate, maxDate)

    override fun getPieChartData(minDate: Int, maxDate: Int): Flow<List<PieChartData>> = flow {
        emit(
            calculatePercentage(
                recordsDao.sumOfMoneyGroupByCategory(
                    fromDate = minDate,
                    toDate = maxDate
                )
            )
        )
    }

    private fun calculatePercentage(values: List<PieChartData>): List<PieChartData> {
        val sumOfAllExpenses =
            values.filter { it.categoryType == Constants.EXPENSES_TYPE_MARKER }
                .sumOf { it.sumOfMoney }

        val sumOfAllIncome =
            values.filter { it.categoryType == Constants.INCOME_TYPE_MARKER }
                .sumOf { it.sumOfMoney }

        val newList = ArrayList<PieChartData>()

        for (item in values) {
            val percentage: Double = when (item.categoryType) {
                Constants.EXPENSES_TYPE_MARKER -> {
                    (item.sumOfMoney.div(sumOfAllExpenses)).times(100)
                }
                Constants.INCOME_TYPE_MARKER -> {
                    (item.sumOfMoney.div(sumOfAllIncome)).times(100)
                }
                else -> {
                    0.0
                }
            }
            newList.add(
                PieChartData(
                    categoryId = item.categoryId,
                    percentage = percentage.roundToOneDigit(),
                    sumOfMoney = item.sumOfMoney,
                    categoryName = item.categoryName,
                    categoryType = item.categoryType,
                    categoryImage = item.categoryImage
                )
            )
        }
        return newList
    }


    override fun getAllTransactionByCategoryId(
        categoryId: Int,
        minDate: Int,
        maxDate: Int
    ): Flow<List<Transaction>> = recordsDao.getAllTransactionByCategoryId(
        categoryId = categoryId,
        fromDate = minDate,
        toDate = maxDate
    )


    fun getString(@StringRes id: Int) = _resources.getString(id)

    override suspend fun insertTransaction(
        stateEvent: InsertTransactionStateEvent.InsertTransaction
    ): DataState<InsertTransactionViewState> {

        val cacheResult = safeCacheCall {
            recordsDao.insertOrReplace(stateEvent.transactionEntity)
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
            recordsDao.insertOrReplace(stateEvent.transactionEntity)
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
            recordsDao.updateRecord(transactionEntity = stateEvent.transactionEntity)
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
            recordsDao.insertOrReplace(stateEvent.transactionEntity)
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
            recordsDao.deleteRecord(stateEvent.transactionEntity)
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
            recordsDao.deleteRecord(stateEvent.transactionId)
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
            recordsDao.deleteRecord(stateEvent.transactionId)
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
            recordsDao.getTransactionById(stateEvent.transactionId)
        }

        return object : CacheResponseHandler<DetailEditTransactionViewState, Transaction>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Transaction): DataState<DetailEditTransactionViewState> {
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