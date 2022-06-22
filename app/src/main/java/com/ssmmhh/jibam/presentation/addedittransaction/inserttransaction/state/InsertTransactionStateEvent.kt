package com.ssmmhh.jibam.presentation.addedittransaction.inserttransaction.state

import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.Transaction
import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.presentation.common.state.InsertNewTransactionStateEvent
import com.ssmmhh.jibam.util.StateEvent

sealed class InsertTransactionStateEvent : StateEvent {

    data class InsertTransaction(
        override val transaction: Transaction
    ) : InsertTransactionStateEvent(), InsertNewTransactionStateEvent {
        override val errorInfo: Int = R.string.unable_to_insert_transaction

        override val getId: String =
            "InsertTransaction $transaction + ${this.hashCode()}"

    }

    object GetAllOfCategories : InsertTransactionStateEvent() {
        override val errorInfo: Int = R.string.unable_to_get_all_of_categories

        override val getId: String =
            "GetAllOfCategories time: hash: ${this.hashCode()}"
    }
}