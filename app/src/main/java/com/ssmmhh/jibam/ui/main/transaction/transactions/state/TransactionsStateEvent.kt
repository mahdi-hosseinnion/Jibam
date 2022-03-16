package com.ssmmhh.jibam.ui.main.transaction.transactions.state

import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.util.StateEvent

sealed class TransactionsStateEvent : StateEvent {

    data class InsertTransaction(
        val transactionEntity: TransactionEntity
    ) : TransactionsStateEvent() {
        override fun errorInfo(): String = "Unable to insert transaction"

        override fun getId(): String =
            "InsertTransaction $transactionEntity + ${this.hashCode()}"

    }

    data class DeleteTransaction(
        val transactionEntity: TransactionEntity,
        val showSuccessToast: Boolean = true
    ) : TransactionsStateEvent() {
        override fun errorInfo(): String =
            "Unable to delete transaction"

        override fun getId(): String =
            "DeleteTransaction $transactionEntity hash: ${this.hashCode()} " +
                    "showSuccessToast: $showSuccessToast"
    }

}