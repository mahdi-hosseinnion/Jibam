package com.ssmmhh.jibam.feature_transactions.state

import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.ui.main.transaction.feature_common.state.DeleteTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.feature_common.state.InsertNewTransactionStateEvent
import com.ssmmhh.jibam.util.StateEvent

sealed class TransactionsStateEvent : StateEvent {

    data class InsertTransaction(
        override val transactionEntity: TransactionEntity
    ) : TransactionsStateEvent(), InsertNewTransactionStateEvent {
        override fun errorInfo(): String = "Unable to insert transaction"

        override fun getId(): String =
            "InsertTransaction $transactionEntity + ${this.hashCode()}"

    }

    data class DeleteTransaction(
        override val transactionId: Int,
        override val showSuccessToast: Boolean = true
    ) : TransactionsStateEvent(), DeleteTransactionStateEvent {

        override fun errorInfo(): String =
            "Unable to delete transaction"

        override fun getId(): String =
            "DeleteTransaction $transactionId hash: ${this.hashCode()} " +
                    "showSuccessToast: $showSuccessToast"
    }

}