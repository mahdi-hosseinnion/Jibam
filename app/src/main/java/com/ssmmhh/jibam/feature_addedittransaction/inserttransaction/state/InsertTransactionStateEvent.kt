package com.ssmmhh.jibam.feature_addedittransaction.inserttransaction.state

import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.feature_common.state.InsertNewTransactionStateEvent
import com.ssmmhh.jibam.util.StateEvent

sealed class InsertTransactionStateEvent : StateEvent {

    data class InsertTransaction(
        override val transactionEntity: TransactionEntity
    ) : InsertTransactionStateEvent(), InsertNewTransactionStateEvent {
        override val errorInfo: String = "Unable to insert transaction"

        override val getId: String =
            "InsertTransaction $transactionEntity + ${this.hashCode()}"

    }

    object GetAllOfCategories : InsertTransactionStateEvent() {
        override val errorInfo: String = "Unable to get all of categories"

        override val getId: String =
            "GetAllOfCategories time: hash: ${this.hashCode()}"
    }
}