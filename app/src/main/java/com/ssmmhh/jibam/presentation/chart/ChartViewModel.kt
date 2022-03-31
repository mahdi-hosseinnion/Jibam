package com.ssmmhh.jibam.presentation.chart

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ssmmhh.jibam.data.model.ChartData
import com.ssmmhh.jibam.data.model.Month
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.data.source.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.presentation.chart.state.ChartStateEvent
import com.ssmmhh.jibam.presentation.chart.state.ChartViewState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.presentation.common.MonthManger
import com.ssmmhh.jibam.data.util.DataState
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


    private val _pieChartData: LiveData<List<ChartData>> =
        monthManger.currentMonth.flatMapLatest {
            setCurrentMonth(it)
            transactionRepository.getPieChartData(
                fromDate = it.startOfMonth,
                toDate = it.endOfMonth
            )
        }.asLiveData()

    val pieChartData: LiveData<List<ChartData>> = _pieChartData

    fun getAllTransactionByCategoryId(categoryId: Int): LiveData<List<TransactionDto>> =
        monthManger.currentMonth.flatMapLatest {
            transactionRepository.getAllTransactionByCategoryId(
                categoryId = categoryId,
                fromDate = it.startOfMonth,
                toDate = it.endOfMonth
            )
        }.asLiveData()

    override fun initNewViewState(): ChartViewState = ChartViewState()

    override suspend fun getResultByStateEvent(stateEvent: ChartStateEvent): DataState<ChartViewState> =
        when (stateEvent) {
            is ChartStateEvent.DeleteTransaction -> {
                val result = transactionRepository.deleteTransaction(
                    stateEvent
                )
                DataState(
                    stateMessage = result.stateMessage,
                    data = null,
                    stateEvent = result.stateEvent
                )
            }
            is ChartStateEvent.InsertTransaction -> {
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
            recentlyDeletedTransaction = recentlyDeletedTransaction,
            currentMonth = newViewState.currentMonth ?: outDate.currentMonth,
        )
    }

    fun setRecentlyDeletedTrans(recentlyDeletedTransaction: TransactionDto) {
        setViewState(
            ChartViewState(
                recentlyDeletedTransaction = recentlyDeletedTransaction
            )
        )
    }

    fun getRecentlyDeletedTrans(): TransactionDto? = viewState.value?.recentlyDeletedTransaction

    fun deleteTransaction(transactionId: Int) {
        launchNewJob(
            ChartStateEvent.DeleteTransaction(
                transactionId = transactionId,
                showSuccessToast = false
            )
        )
    }

    fun insertRecentlyDeletedTrans(transaction: TransactionDto) {

        launchNewJob(
            ChartStateEvent.InsertTransaction(
                transactionEntity = transaction.toTransactionEntity()
            )
        )
    }

    private fun setCurrentMonth(month: Month) {
        setViewState(
            ChartViewState(currentMonth = month)
        )
    }

    fun showMonthPickerBottomSheet(parentFragmentManager: FragmentManager) {
        monthManger.showMonthPickerBottomSheet(parentFragmentManager)
    }

    fun navigateToPreviousMonth() {
        monthManger.navigateToPreviousMonth()
    }

    fun navigateToNextMonth() {
        monthManger.navigateToNextMonth()
    }

    companion object {
        const val FORCE_TO_NULL = "FORCE THIS TO NULL"
    }
}