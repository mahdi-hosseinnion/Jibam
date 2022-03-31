package com.ssmmhh.jibam.presentation.addedittransaction.detailedittransaction.state

import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.presentation.common.state.DeleteTransactionStateEvent
import com.ssmmhh.jibam.util.StateEvent

sealed class DetailEditTransactionStateEvent : StateEvent {

    data class GetTransactionById(
        val transactionId: Int
    ) : DetailEditTransactionStateEvent() {

        override val errorInfo: String = "ERROR: getting transaction!"

        override val getId: String =
            "GetSpecificTransaction id: $transactionId ${this.hashCode()}"
    }

    object GetAllOfCategories : DetailEditTransactionStateEvent() {
        override val errorInfo: String = "Unable to get all of categories"

        override val getId: String =
            "GetAllOfCategories time: hash: ${this.hashCode()}"
    }

    data class UpdateTransaction(
        val transactionEntity: TransactionEntity
    ) : DetailEditTransactionStateEvent() {
        override val errorInfo: String =
            "Unable to update transaction"

        override val getId: String =
            "UpdateTransaction $transactionEntity hash: ${this.hashCode()} "
    }

    data class DeleteTransaction(
        override val transactionId: Int,
        override val showSuccessToast: Boolean = true
    ) : DetailEditTransactionStateEvent(), DeleteTransactionStateEvent {
        override val errorInfo: String =
            "Unable to delete transaction"

        override val getId: String =
            "DeleteTransaction $transactionId hash: ${this.hashCode()} " +
                    "showSuccessToast: $showSuccessToast"
    }
}