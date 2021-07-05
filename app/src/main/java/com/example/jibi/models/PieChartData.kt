package com.example.jibi.models

import androidx.room.ColumnInfo

data class PieChartData(
    @ColumnInfo(name = "percentage")
    val percentage: Double? = -1.0,
    @ColumnInfo(name = "sumOfMoney")
    val sumOfMoney: Double,
    @ColumnInfo(name = "categoryName")
    val categoryName: String,
    @ColumnInfo(name = "categoryType")
    val categoryType: Int,
    @ColumnInfo(name = "categoryImage")
    val categoryImage: String,
)