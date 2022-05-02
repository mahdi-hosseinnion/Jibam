package com.ssmmhh.jibam.presentation.chart.detailchart

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.data.source.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.presentation.chart.detailchart.state.DetailChartStateEvent
import com.ssmmhh.jibam.presentation.chart.detailchart.state.DetailChartViewState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.presentation.common.MonthManger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class DetailChartViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val monthManger: MonthManger
) : BaseViewModel<DetailChartViewState, DetailChartStateEvent>() {

    fun getAllTransactionByCategoryId(categoryId: Int): LiveData<List<TransactionDto>> =
        monthManger.currentMonth.flatMapLatest {
            transactionRepository.getAllTransactionByCategoryId(
                categoryId = categoryId,
                fromDate = it.startOfMonth,
                toDate = it.endOfMonth
            )
        }.asLiveData()

    override fun initNewViewState(): DetailChartViewState = DetailChartViewState()

    override suspend fun getResultByStateEvent(stateEvent: DetailChartStateEvent): DataState<DetailChartViewState> =
        when (stateEvent) {
            is DetailChartStateEvent.DeleteTransaction -> {
                val result = transactionRepository.deleteTransaction(
                    stateEvent
                )
                DataState(
                    stateMessage = result.stateMessage,
                    data = null,
                    stateEvent = result.stateEvent
                )
            }
            is DetailChartStateEvent.InsertTransaction -> {
                val result = transactionRepository.insertTransaction(
                    stateEvent
                )
                DataState(
                    stateMessage = result.stateMessage,
                    data = null,
                    stateEvent = result.stateEvent
                )
            }
        }

    override fun updateViewState(newViewState: DetailChartViewState): DetailChartViewState {
        val outDate = getCurrentViewStateOrNew()
        //we should force this to null if user didn't want to restore transaction
        val recentlyDeletedTransaction =
            if (newViewState.recentlyDeletedTransaction?.memo == FORCE_TO_NULL)
                null
            else
                newViewState.recentlyDeletedTransaction
                    ?: outDate.recentlyDeletedTransaction

        return DetailChartViewState(
            recentlyDeletedTransaction = recentlyDeletedTransaction,
        )
    }

    fun setRecentlyDeletedTrans(recentlyDeletedTransaction: TransactionDto) {
        setViewState(
            DetailChartViewState(
                recentlyDeletedTransaction = recentlyDeletedTransaction
            )
        )
    }

    fun getRecentlyDeletedTrans(): TransactionDto? = viewState.value?.recentlyDeletedTransaction

    fun deleteTransaction(transactionId: Int) {
        launchNewJob(
            DetailChartStateEvent.DeleteTransaction(
                transactionId = transactionId,
                showSuccessToast = false
            )
        )
    }

    fun insertRecentlyDeletedTrans(transaction: TransactionDto) {

        launchNewJob(
            DetailChartStateEvent.InsertTransaction(
                transactionEntity = transaction.toTransactionEntity()
            )
        )
    }

    companion object {
        const val FORCE_TO_NULL = "FORCE THIS TO NULL"
    }
}