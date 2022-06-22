package com.ssmmhh.jibam.presentation.chart.detailchart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.data.source.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.data.util.*
import com.ssmmhh.jibam.presentation.chart.detailchart.state.DetailChartStateEvent
import com.ssmmhh.jibam.presentation.chart.detailchart.state.DetailChartViewState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.presentation.common.MonthManger
import com.ssmmhh.jibam.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class DetailChartViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    monthManger: MonthManger
) : BaseViewModel<DetailChartViewState, DetailChartStateEvent>() {

    // Contains transaction's category id to query them from db.
    private val _categoryId = MutableLiveData<Int?>(null)
    val categoryId: LiveData<Int?> = _categoryId

    val transactions: LiveData<List<TransactionDto>> = combine(
        monthManger.currentMonth,
        categoryId.asFlow()
    ) { month, categoryId ->
        return@combine Pair(first = month, second = categoryId)
    }.flatMapLatest {
        it.second?.let { categoryId ->
            transactionRepository.getAllTransactionByCategoryId(
                categoryId = categoryId,
                fromDate = it.first.startOfMonth,
                toDate = it.first.endOfMonth
            )
        } ?: emptyFlow()
    }.asLiveData()

    private val _navigateToTransactionDetail = MutableLiveData<Event<Int>>()
    val navigateToTransactionDetail: LiveData<Event<Int>> = _navigateToTransactionDetail

    //Contains the transaction that user deleted by swiping for snack bar 'undo' action.
    private var deletedTransaction: TransactionDto? = null

    fun start(categoryId: Int) {
        _categoryId.value = categoryId
    }

    fun openTransactionDetail(item: TransactionDto) {
        _navigateToTransactionDetail.value = Event(item.id)
    }

    fun deleteTransaction(transactionToDelete: TransactionDto) {
        deletedTransaction = transactionToDelete

        launchNewJob(
            DetailChartStateEvent.DeleteTransaction(
                transactionId = transactionToDelete.id,
                showSuccessToast = false
            )
        )
    }

    fun insertTransaction(transaction: TransactionDto) {

       /* launchNewJob(
            DetailChartStateEvent.InsertTransaction(
                transactionEntity = transaction.toTransactionEntity()
            )
        )*/
    }

    override suspend fun getResultByStateEvent(stateEvent: DetailChartStateEvent): DataState<DetailChartViewState> =
        when (stateEvent) {
            is DetailChartStateEvent.DeleteTransaction -> {
                val result = transactionRepository.deleteTransaction(
                    stateEvent
                )
                DataState(
                    stateMessage = getUndoSnackBarStateMessageForDeleteTransaction(),
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

    private fun getUndoSnackBarStateMessageForDeleteTransaction(): StateMessage {
        val undoCallback = object : UndoCallback {
            override fun undo() {

                deletedTransaction?.let {
                    //Insert deleted transaction
                    insertTransaction(it)
                } ?: run {
                    //Show error
                    addToMessageStack(
                        message = intArrayOf(R.string.unable_to_restore_transaction),
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    )
                }
            }

            override fun onDismiss() {
                //If you set [deletedTransactionItem] to null here there gone be bug when deleting
                //multiple item fast.
            }
        }

        return StateMessage(
            response = Response(
                intArrayOf(R.string.transaction_successfully_deleted),
                UIComponentType.UndoSnackBar(undoCallback),
                MessageType.Info
            )
        )
    }

    override fun updateViewState(newViewState: DetailChartViewState): DetailChartViewState =
        DetailChartViewState()

    override fun initNewViewState(): DetailChartViewState = DetailChartViewState()

}