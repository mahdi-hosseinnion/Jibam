package com.ssmmhh.jibam.ui.main.transaction.common.state

import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.util.StateEvent

interface InsertNewTransactionStateEvent : StateEvent {
    val transactionEntity: TransactionEntity
}
