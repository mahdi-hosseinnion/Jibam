package com.example.jibi.models

import androidx.room.ColumnInfo

data class PieChartData(
    @ColumnInfo(name = "sumOfMoney")
    val sumOfMoney: Double,
    @ColumnInfo(name = "categoryName")
    val categoryName: String,
    @ColumnInfo(name = "categoryType")
    val categoryType: Int
)