package com.ssmmhh.jibam.presentation.chart.state

import com.ssmmhh.jibam.data.model.Month
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto

data class ChartViewState(
    val recentlyDeletedTransaction: TransactionDto? = null,
    val currentMonth: Month? = null
)