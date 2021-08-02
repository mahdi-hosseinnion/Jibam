package com.example.jibi.ui.main.transaction.addedittransaction.state

import com.example.jibi.models.TransactionEntity
import com.example.jibi.util.StateEvent

sealed class AddEditTransactionStateEvent : StateEvent {


    data class InsertTransaction(
        val transactionEntity: TransactionEntity
    ) : AddEditTransactionStateEvent() {
        override fun errorInfo(): String = "Unable to insert transaction"

        override fun getId(): String =
            "InsertTransaction $transactionEntity + ${this.hashCode()}"

    }

    data class DeleteTransaction(
        val transactionId: Int,
        val showSuccessToast: Boolean = true
    ) : AddEditTransactionStateEvent() {
        override fun errorInfo(): String =
            "Unable to delete transaction"

        override fun getId(): String =
            "DeleteTransaction $transactionId hash: ${this.hashCode()} " +
                    "showSuccessToast: $showSuccessToast"
    }


    data class GetTransactionById(
        val transactionId: Int
    ) : AddEditTransactionStateEvent() {
        override fun errorInfo(): String = "ERROR: getting transaction!"

        override fun getId(): String =
            "GetSpecificTransaction id: $transactionId ${this.hashCode()}"
    }

    object GetAllOfCategories : AddEditTransactionStateEvent() {
        override fun errorInfo(): String = "Unable to get all of categories"

        override fun getId(): String =
            "GetSpecificTransaction time: ${System.currentTimeMillis()} ${this.hashCode()}"
    }

}
