package com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state

import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.ui.main.transaction.common.state.InsertNewTransactionStateEvent
import com.ssmmhh.jibam.util.StateEvent

sealed class InsertTransactionStateEvent : StateEvent {

    data class InsertTransaction(
        override val transactionEntity: TransactionEntity
    ) : InsertTransactionStateEvent(),InsertNewTransactionStateEvent{
        override fun errorInfo(): String = "Unable to insert transaction"

        override fun getId(): String =
            "InsertTransaction $transactionEntity + ${this.hashCode()}"

    }

    object GetAllOfCategories : InsertTransactionStateEvent() {
        override fun errorInfo(): String = "Unable to get all of categories"

        override fun getId(): String =
            "GetAllOfCategories time: hash: ${this.hashCode()}"
    }
}