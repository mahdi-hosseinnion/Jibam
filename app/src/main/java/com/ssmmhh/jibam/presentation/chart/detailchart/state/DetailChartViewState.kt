package com.ssmmhh.jibam.presentation.chart.detailchart.state

import com.ssmmhh.jibam.data.source.local.dto.TransactionDto

data class DetailChartViewState(
    val recentlyDeletedTransaction: TransactionDto? = null,
)