package com.ssmmhh.jibam.presentation.transactiondetail.state

import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.presentation.common.state.DeleteTransactionStateEvent
import com.ssmmhh.jibam.util.StateEvent

sealed class TransactionDetailStateEvent : StateEvent {

    class DeleteTransaction(
        override val transactionId: Int,
        override val showSuccessToast: Boolean = true,
    ) : TransactionDetailStateEvent(), DeleteTransactionStateEvent {
        override val errorInfo: Int
            get() = R.string.unable_to_delete_transaction
        override val getId: String
            get() = "TransactionDetailStateEvent: DeleteTransaction: id: $transactionId ${this.hashCode()}"

    }
}