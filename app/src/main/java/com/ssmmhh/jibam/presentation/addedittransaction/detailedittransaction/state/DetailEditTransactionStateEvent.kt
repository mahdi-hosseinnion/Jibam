package com.ssmmhh.jibam.presentation.addedittransaction.detailedittransaction.state

import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.presentation.common.state.DeleteTransactionStateEvent
import com.ssmmhh.jibam.util.StateEvent

sealed class DetailEditTransactionStateEvent : StateEvent {

    data class GetTransactionById(
        val transactionId: Int
    ) : DetailEditTransactionStateEvent() {

        override val errorInfo: Int = R.string.unable_to_get_the_transaction

        override val getId: String =
            "GetSpecificTransaction id: $transactionId ${this.hashCode()}"
    }

    object GetAllOfCategories : DetailEditTransactionStateEvent() {
        override val errorInfo: Int = R.string.unable_to_get_all_of_categories

        override val getId: String =
            "GetAllOfCategories time: hash: ${this.hashCode()}"
    }

    data class UpdateTransaction(
        val transactionEntity: TransactionEntity
    ) : DetailEditTransactionStateEvent() {
        override val errorInfo: Int =
            R.string.unable_to_update_transaction

        override val getId: String =
            "UpdateTransaction $transactionEntity hash: ${this.hashCode()} "
    }

    data class DeleteTransaction(
        override val transactionId: Int,
        override val showSuccessToast: Boolean = true
    ) : DetailEditTransactionStateEvent(), DeleteTransactionStateEvent {
        override val errorInfo: Int =
            R.string.unable_to_delete_transaction

        override val getId: String =
            "DeleteTransaction $transactionId hash: ${this.hashCode()} " +
                    "showSuccessToast: $showSuccessToast"
    }
}