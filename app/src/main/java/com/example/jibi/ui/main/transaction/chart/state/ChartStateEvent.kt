package com.example.jibi.ui.main.transaction.chart.state

import com.example.jibi.models.TransactionEntity
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.example.jibi.util.StateEvent

sealed class ChartStateEvent : StateEvent {

    data class DeleteTransaction(
        val transactionId: Int,
        val showSuccessToast: Boolean = true
    ) : ChartStateEvent() {
        override fun errorInfo(): String =
            "Unable to delete transaction"

        override fun getId(): String =
            "DeleteTransaction $transactionId hash: ${this.hashCode()} " +
                    "showSuccessToast: $showSuccessToast"
    }

    data class InsertTransaction(
        val transactionEntity: TransactionEntity
    ) : ChartStateEvent() {
        override fun errorInfo(): String = "Unable to insert transaction"

        override fun getId(): String =
            "InsertTransaction $transactionEntity + ${this.hashCode()}"

    }

}