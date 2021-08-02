package com.example.jibi.ui.main.transaction.chart

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.jibi.models.PieChartData
import com.example.jibi.models.Transaction
import com.example.jibi.repository.tranasction.TransactionRepository
import com.example.jibi.ui.main.transaction.MonthManger
import com.example.jibi.ui.main.transaction.chart.state.ChartStateEvent
import com.example.jibi.ui.main.transaction.chart.state.ChartViewState
import com.example.jibi.ui.main.transaction.common.NewBaseViewModel
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsViewState
import com.example.jibi.util.DataState
import com.example.jibi.util.MessageType
import com.example.jibi.util.Response
import com.example.jibi.util.UIComponentType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

@ExperimentalCoroutinesApi
class ChartViewModel
constructor(
    private val transactionRepository: TransactionRepository,
    private val monthManger: MonthManger
) : NewBaseViewModel<ChartViewState, ChartStateEvent>() {


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
            else -> DataState.error(
                Response(
                    message = "UNKNOWN STATE EVENT ",
                    uiComponentType = UIComponentType.Toast,
                    messageType = MessageType.Error
                )
            )
        }

    override fun updateViewState(newViewState: ChartViewState): ChartViewState {
        val outDate = getCurrentViewStateOrNew()
        return ChartViewState(
        )
    }
}