package com.ssmmhh.jibam.ui.main.transaction.chart.state

import com.ssmmhh.jibam.models.Month
import com.ssmmhh.jibam.models.Transaction

data class ChartViewState(
    val recentlyDeletedTransaction: Transaction? = null,
    val currentMonth: Month? = null
)