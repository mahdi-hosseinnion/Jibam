package com.ssmmhh.jibam.presentation.common.state

import com.ssmmhh.jibam.util.StateEvent

interface DeleteTransactionStateEvent : StateEvent {
    val transactionId: Int
    val showSuccessToast: Boolean
}
