package com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state

import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.util.StateEvent

sealed class DetailEditTransactionStateEvent : StateEvent {

    data class GetTransactionById(
        val transactionId: Int
    ) : DetailEditTransactionStateEvent() {

        override fun errorInfo(): String = "ERROR: getting transaction!"

        override fun getId(): String =
            "GetSpecificTransaction id: $transactionId ${this.hashCode()}"
    }

    object GetAllOfCategories : DetailEditTransactionStateEvent() {
        override fun errorInfo(): String = "Unable to get all of categories"

        override fun getId(): String =
            "GetAllOfCategories time: hash: ${this.hashCode()}"
    }

    data class UpdateTransaction(
        val transactionEntity: TransactionEntity
    ) : DetailEditTransactionStateEvent() {
        override fun errorInfo(): String =
            "Unable to update transaction"

        override fun getId(): String =
            "UpdateTransaction $transactionEntity hash: ${this.hashCode()} "
    }

    data class DeleteTransaction(
        val transactionId: Int,
        val showSuccessToast: Boolean = true
    ) : DetailEditTransactionStateEvent() {
        override fun errorInfo(): String =
            "Unable to delete transaction"

        override fun getId(): String =
            "DeleteTransaction $transactionId hash: ${this.hashCode()} " +
                    "showSuccessToast: $showSuccessToast"
    }
}