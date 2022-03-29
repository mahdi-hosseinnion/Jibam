package com.ssmmhh.jibam.feature_common.state

import com.ssmmhh.jibam.util.StateEvent

interface DeleteTransactionStateEvent : StateEvent {
    val transactionId: Int
    val showSuccessToast: Boolean
}
