package com.ssmmhh.jibam.presentation.chart.chart

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
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
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class ChartViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val monthManger: MonthManger
) : BaseViewModel<ChartViewState, ChartStateEvent>() {

    private val _isChartTypeExpenses: MutableLiveData<Boolean> = MutableLiveData(true)
    val isChartTypeExpenses: LiveData<Boolean> = _isChartTypeExpenses

    private val _pieChartData: LiveData<List<ChartData>> = combine(
        monthManger.currentMonth,
        isChartTypeExpenses.asFlow()
    ) { month, _ ->
        setCurrentMonth(month)
        return@combine (month)
    }.flatMapLatest {

        transactionRepository.getPieChartData(
            fromDate = it.startOfMonth,
            toDate = it.endOfMonth
        ).map { list ->
            list.filter { chart ->
                if (isChartTypeExpenses.value == true) {
                    chart.isExpensesCategory
                } else {
                    chart.isIncomeCategory
                }
            }

        }
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

    /**
     * Reverse chart category type.
     */
    fun swapChartType() {
        val oldValue: Boolean = _isChartTypeExpenses.value ?: true
        _isChartTypeExpenses.value = !oldValue
    }

    companion object {
        const val FORCE_TO_NULL = "FORCE THIS TO NULL"
    }
}