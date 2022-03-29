package com.ssmmhh.jibam.feature_common.state

import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.util.StateEvent

interface InsertNewTransactionStateEvent : StateEvent {
    val transactionEntity: TransactionEntity
}
