package com.ssmmhh.jibam.feature_addedittransaction.detailedittransaction.state

import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.ui.main.transaction.feature_common.state.DeleteTransactionStateEvent
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
        override val transactionId: Int,
        override val showSuccessToast: Boolean = true
    ) : DetailEditTransactionStateEvent(), DeleteTransactionStateEvent {
        override fun errorInfo(): String =
            "Unable to delete transaction"

        override fun getId(): String =
            "DeleteTransaction $transactionId hash: ${this.hashCode()} " +
                    "showSuccessToast: $showSuccessToast"
    }
}