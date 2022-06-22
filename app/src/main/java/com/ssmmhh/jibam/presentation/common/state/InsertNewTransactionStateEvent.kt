package com.ssmmhh.jibam.presentation.common.state

import android.view.SurfaceControl
import com.ssmmhh.jibam.data.model.Transaction
import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.util.StateEvent

interface InsertNewTransactionStateEvent : StateEvent {
    val transaction: Transaction
}
