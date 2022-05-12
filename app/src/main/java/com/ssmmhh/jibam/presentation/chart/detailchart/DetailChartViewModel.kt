package com.ssmmhh.jibam.presentation.chart.detailchart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _navigateToTransactionDetail = MutableLiveData<Event<Int>>()
    val navigateToTransactionDetail: LiveData<Event<Int>> = _navigateToTransactionDetail

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

                getDeletedTransaction()?.let {
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

            override fun onDismiss() {}
        }

        return StateMessage(
            response = Response(
                intArrayOf(R.string.transaction_successfully_deleted),
                UIComponentType.UndoSnackBar(undoCallback),
                MessageType.Info
            )
        )
    }


    override fun updateViewState(newViewState: DetailChartViewState): DetailChartViewState {
        val outDate = getCurrentViewStateOrNew()
        return DetailChartViewState(
            recentlyDeletedTransaction = newViewState.recentlyDeletedTransaction
                ?: outDate.recentlyDeletedTransaction
        )
    }

    private fun setRecentlyDeletedTrans(recentlyDeletedTransaction: TransactionDto) {
        setViewState(
            DetailChartViewState(
                recentlyDeletedTransaction = recentlyDeletedTransaction
            )
        )
    }

    fun getDeletedTransaction(): TransactionDto? = viewState.value?.recentlyDeletedTransaction

    fun deleteTransaction(transactionId: Int) {
        launchNewJob(
            DetailChartStateEvent.DeleteTransaction(
                transactionId = transactionId,
                showSuccessToast = false
            )
        )
    }

    fun insertTransaction(transaction: TransactionDto) {

        launchNewJob(
            DetailChartStateEvent.InsertTransaction(
                transactionEntity = transaction.toTransactionEntity()
            )
        )
    }

    fun deleteTransaction(transactionToDelete: TransactionDto?) {
        if (transactionToDelete == null) {
            //show error to user
            return
        }
        //add to recently deleted
        setRecentlyDeletedTrans(
            transactionToDelete
        )
        //delete from database
        deleteTransaction(transactionToDelete.id)
        //show snackBar
    }

    fun openTransactionDetail(item: TransactionDto) {
        _navigateToTransactionDetail.value = Event(item.id)
    }
}