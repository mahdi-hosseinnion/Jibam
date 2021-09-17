package com.example.jibi.ui.main.transaction.chart

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.jibi.models.PieChartData
import com.example.jibi.models.Transaction
import com.example.jibi.models.mappers.toTransactionEntity
import com.example.jibi.repository.tranasction.TransactionRepository
import com.example.jibi.ui.main.transaction.MonthManger
import com.example.jibi.ui.main.transaction.chart.state.ChartStateEvent
import com.example.jibi.ui.main.transaction.chart.state.ChartViewState
import com.example.jibi.ui.main.transaction.common.BaseViewModel
import com.example.jibi.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
//@MainScope
class ChartViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val monthManger: MonthManger
) : BaseViewModel<ChartViewState, ChartStateEvent>() {


    private val _pieChartData: LiveData<List<PieChartData>> =
        monthManger.currentMonth.flatMapLatest {
            transactionRepository.getPieChartData(
                minDate = it.startOfMonth,
                maxDate = it.endOfMonth
            )
        }.asLiveData()

    val pieChartData: LiveData<List<PieChartData>> = _pieChartData

    fun getAllTransactionByCategoryId(categoryId: Int): LiveData<List<Transaction>> =
        monthManger.currentMonth.flatMapLatest {
            transactionRepository.getAllTransactionByCategoryId(
                categoryId = categoryId,
                minDate = it.startOfMonth,
                maxDate = it.endOfMonth
            )
        }.asLiveData()

    override fun initNewViewState(): ChartViewState = ChartViewState()

    override suspend fun getResultByStateEvent(stateEvent: ChartStateEvent): DataState<ChartViewState> =
        when (stateEvent) {
            is ChartStateEvent.DeleteTransaction -> transactionRepository.deleteTransaction(
                stateEvent
            )
            is ChartStateEvent.InsertTransaction -> transactionRepository.insertTransaction(
                stateEvent
            )
        }

    override fun updateViewState(newViewState: ChartViewState): ChartViewState {
        val outDate = getCurrentViewStateOrNew()
        //we should force this to null if user didn't want to restore transaction
        val recentlyDeletedTransaction =
            if (newViewState.recentlyDeletedTransaction?.memo == FORCE_TO_NULL)
                null
            else
                newViewState.recentlyDeletedTransaction
                    ?: outDate.recentlyDeletedTransaction

        return ChartViewState(
            recentlyDeletedTransaction = recentlyDeletedTransaction
        )
    }

    fun setRecentlyDeletedTrans(recentlyDeletedTransaction: Transaction) {
        setViewState(
            ChartViewState(
                recentlyDeletedTransaction = recentlyDeletedTransaction
            )
        )
    }

    fun getRecentlyDeletedTrans(): Transaction? = viewState.value?.recentlyDeletedTransaction

    fun deleteTransaction(transactionId: Int) {
        launchNewJob(
            ChartStateEvent.DeleteTransaction(
                transactionId = transactionId,
                showSuccessToast = false
            )
        )
    }

    fun insertRecentlyDeletedTrans(transaction: Transaction) {

        launchNewJob(
            ChartStateEvent.InsertTransaction(
                transactionEntity = transaction.toTransactionEntity()
            )
        )
    }

    companion object {
        const val FORCE_TO_NULL = "FORCE THIS TO NULL"
    }
}