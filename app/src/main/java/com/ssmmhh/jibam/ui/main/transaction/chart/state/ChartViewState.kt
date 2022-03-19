package com.ssmmhh.jibam.ui.main.transaction.chart.state

import com.ssmmhh.jibam.models.Month
import com.ssmmhh.jibam.persistence.dtos.TransactionDto

data class ChartViewState(
    val recentlyDeletedTransaction: TransactionDto? = null,
    val currentMonth: Month? = null
)