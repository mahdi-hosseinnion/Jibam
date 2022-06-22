package com.ssmmhh.jibam.presentation.addedittransaction.state

import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.Transaction
import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.presentation.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.ssmmhh.jibam.presentation.common.state.InsertNewTransactionStateEvent
import com.ssmmhh.jibam.util.StateEvent

sealed class AddEditTransactionStateEvent : StateEvent {

    data class InsertTransaction(
        override val transaction: Transaction
    ) : AddEditTransactionStateEvent(), InsertNewTransactionStateEvent {
        override val errorInfo: Int = R.string.unable_to_insert_transaction

        override val getId: String =
            "InsertTransaction $transaction + ${this.hashCode()}"

    }
}
