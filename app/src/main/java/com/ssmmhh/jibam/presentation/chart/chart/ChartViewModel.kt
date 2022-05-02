package com.ssmmhh.jibam.presentation.chart.chart

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ssmmhh.jibam.data.model.ChartData
import com.ssmmhh.jibam.data.model.Month
import com.ssmmhh.jibam.data.source.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.presentation.chart.chart.state.ChartStateEvent
import com.ssmmhh.jibam.presentation.chart.chart.state.ChartViewState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.presentation.common.MonthManger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
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

    override fun initNewViewState(): ChartViewState = ChartViewState()

    override suspend fun getResultByStateEvent(stateEvent: ChartStateEvent)
            : DataState<ChartViewState> = DataState(stateEvent = stateEvent)

    override fun updateViewState(newViewState: ChartViewState): ChartViewState {
        val outDate = getCurrentViewStateOrNew()
        return ChartViewState(
            currentMonth = newViewState.currentMonth ?: outDate.currentMonth,
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