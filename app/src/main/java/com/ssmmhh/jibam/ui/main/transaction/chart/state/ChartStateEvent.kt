package com.ssmmhh.jibam.ui.main.transaction.chart.state

import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.ui.main.transaction.common.state.DeleteTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.common.state.InsertNewTransactionStateEvent
import com.ssmmhh.jibam.util.StateEvent

sealed class ChartStateEvent : StateEvent {

    data class DeleteTransaction(
        override val transactionId: Int,
        override val showSuccessToast: Boolean = true
    ) : ChartStateEvent(), DeleteTransactionStateEvent {
        override fun errorInfo(): String =
            "Unable to delete transaction"

        override fun getId(): String =
            "DeleteTransaction $transactionId hash: ${this.hashCode()} " +
                    "showSuccessToast: $showSuccessToast"
    }

    data class InsertTransaction(
        override val transactionEntity: TransactionEntity
    ) : ChartStateEvent(), InsertNewTransactionStateEvent {
        override fun errorInfo(): String = "Unable to insert transaction"

        override fun getId(): String =
            "InsertTransaction $transactionEntity + ${this.hashCode()}"

    }

}