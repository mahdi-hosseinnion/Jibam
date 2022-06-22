package com.ssmmhh.jibam.presentation.transactions.state

import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.Transaction
import com.ssmmhh.jibam.presentation.common.state.DeleteTransactionStateEvent
import com.ssmmhh.jibam.presentation.common.state.InsertNewTransactionStateEvent
import com.ssmmhh.jibam.util.StateEvent

sealed class TransactionsStateEvent : StateEvent {

    data class InsertTransaction(
        override val transaction: Transaction
    ) : TransactionsStateEvent(), InsertNewTransactionStateEvent {
        override val errorInfo: Int = R.string.unable_to_insert_transaction

        override val getId: String =
            "InsertTransaction $transaction + ${this.hashCode()}"

    }

    data class DeleteTransaction(
        override val transactionId: Int,
        override val showSuccessToast: Boolean = true
    ) : TransactionsStateEvent(), DeleteTransactionStateEvent {

        override val errorInfo: Int =
            R.string.unable_to_delete_transaction

        override val getId: String =
            "DeleteTransaction $transactionId hash: ${this.hashCode()} " +
                    "showSuccessToast: $showSuccessToast"
    }

}