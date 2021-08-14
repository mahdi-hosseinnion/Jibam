package com.example.jibi.repository.tranasction

import android.content.res.Resources
import android.util.Log
import androidx.annotation.StringRes
import com.example.jibi.R
import com.example.jibi.models.PieChartData
import com.example.jibi.models.Transaction
import com.example.jibi.models.TransactionEntity
import com.example.jibi.persistence.*
import com.example.jibi.repository.buildResponse
import com.example.jibi.repository.safeCacheCall
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionViewState
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsStateEvent
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsViewState
import com.example.jibi.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import java.util.*
import javax.inject.Inject

class TransactionRepositoryImpl
@Inject
constructor(
    private val recordsDao: RecordsDao,
    private val categoriesDao: CategoriesDao,
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
                tryIncreaseCategoryOrdering(stateEvent.transactionEntity.cat_id)
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

    fun getString(@StringRes id: Int) = _resources.getString(id)

    private suspend fun tryIncreaseCategoryOrdering(categoryId: Int) {
        try {
            withTimeout(Constants.CACHE_TIMEOUT.times(2)) {
                val category =
                    categoriesDao.getCategoryById(categoryId)
                if (category == null ||
                    category.ordering < 0 || //if category got pinned
                    category.ordering > Int.MAX_VALUE.minus(2)//if its to big even bigger then int max value
                ) {
                    return@withTimeout
                }
                //increase category ordering
                categoriesDao.updateCategory(category.copy(ordering = category.ordering.plus(1)))
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "tryIncreaseCategoryOrdering: unable to update category order with id:$categoryId",
                e
            )
        }
    }

    override suspend fun insertTransaction(
        stateEvent: AddEditTransactionStateEvent.InsertTransaction
    ): DataState<AddEditTransactionViewState> {

        val cacheResult = safeCacheCall {
            recordsDao.insertOrReplace(stateEvent.transactionEntity)
        }

        return object : CacheResponseHandler<AddEditTransactionViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<AddEditTransactionViewState> {
                tryIncreaseCategoryOrdering(stateEvent.transactionEntity.cat_id)
                return if (resultObj > 0) {
                    DataState.data(
                        response = buildResponse(
                            message = getString(R.string.transaction_successfully_inserted),
                            UIComponentType.Toast,
                            MessageType.Success
                        ),
                        data = AddEditTransactionViewState(
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
        stateEvent: AddEditTransactionStateEvent.DeleteTransaction
    ): DataState<AddEditTransactionViewState> {
        val cacheResult = safeCacheCall {
            recordsDao.deleteRecord(stateEvent.transactionId)
        }
        return object : CacheResponseHandler<AddEditTransactionViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<AddEditTransactionViewState> {
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
                        data = AddEditTransactionViewState(
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

    override suspend fun getTransactionById(
        stateEvent: AddEditTransactionStateEvent.GetTransactionById
    ): DataState<AddEditTransactionViewState> {
        val cacheResult = safeCacheCall {
            recordsDao.getTransactionById(stateEvent.transactionId)
        }

        return object : CacheResponseHandler<AddEditTransactionViewState, Transaction>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Transaction): DataState<AddEditTransactionViewState> {
                return DataState.data(
                    response = buildResponse(
                        message = "Transaction Successfully returned",
                        UIComponentType.None,
                        MessageType.Success
                    ),
                    data = AddEditTransactionViewState(
                        transaction = resultObj
                    )
                )
            }
        }.getResult()
    }


}