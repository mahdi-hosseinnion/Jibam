package com.ssmmhh.jibam.presentation.chart.state

import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.presentation.common.state.DeleteTransactionStateEvent
import com.ssmmhh.jibam.presentation.common.state.InsertNewTransactionStateEvent
import com.ssmmhh.jibam.util.StateEvent

sealed class ChartStateEvent : StateEvent {

    data class DeleteTransaction(
        override val transactionId: Int,
        override val showSuccessToast: Boolean = true
    ) : ChartStateEvent(), DeleteTransactionStateEvent {
        override val errorInfo: Int =
            R.string.unable_to_delete_transaction

        override val getId: String =
            "DeleteTransaction $transactionId hash: ${this.hashCode()} " +
                    "showSuccessToast: $showSuccessToast"
    }

    data class InsertTransaction(
        override val transactionEntity: TransactionEntity
    ) : ChartStateEvent(), InsertNewTransactionStateEvent {
        override val errorInfo: Int = R.string.unable_to_insert_transaction

        override val getId: String =
            "InsertTransaction $transactionEntity + ${this.hashCode()}"

    }

}