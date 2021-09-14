package com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction
/*
import com.example.jibi.models.TransactionEntity
import com.example.jibi.ui.main.transaction.addedittransaction.AddEditTransactionFragment
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.SubmitButtonState
import com.example.jibi.ui.main.transaction.common.BaseViewModel
import com.example.jibi.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*

@ExperimentalCoroutinesApi
@FlowPreview
class DetailEditTransactionViewModel
    :
    BaseViewModel<DetailEditTransactionViewState, DetailEditTransactionStateEvent>() {

    private var submitButtonState: SubmitButtonState? = null

    init {
        setViewState(
            DetailEditTransactionViewState(
                combineCalender = GregorianCalendar(currentLocale)
            )
        )


    }

    override fun initNewViewState(): DetailEditTransactionViewState {
        TODO("Not yet implemented")
    }

    override suspend fun getResultByStateEvent(stateEvent: DetailEditTransactionStateEvent): DataState<DetailEditTransactionViewState> {
        TODO("Not yet implemented")
    }

    override fun updateViewState(newViewState: DetailEditTransactionViewState): DetailEditTransactionViewState {
        TODO("Not yet implemented")
    }

    fun setDefaultTransaction(transactionEntity: TransactionEntity) {
        submitButtonState = SubmitButtonState(transactionEntity)
    }

}*/
