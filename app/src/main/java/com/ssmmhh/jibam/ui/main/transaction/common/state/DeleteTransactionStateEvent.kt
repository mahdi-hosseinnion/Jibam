package com.ssmmhh.jibam.ui.main.transaction.common.state

import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.util.StateEvent

interface DeleteTransactionStateEvent : StateEvent {
    val transactionId: Int
    val showSuccessToast: Boolean
}
