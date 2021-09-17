package com.example.jibi.ui.main.transaction.chart.state

import com.example.jibi.models.Transaction

data class ChartViewState(
    val recentlyDeletedTransaction: Transaction? = null
)