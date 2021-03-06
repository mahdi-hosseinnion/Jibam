package com.ssmmhh.jibam.data.source.local.dto

import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.ssmmhh.jibam.data.model.Image
import com.ssmmhh.jibam.data.model.ChartData
import com.ssmmhh.jibam.data.source.local.typeconverter.BigDecimalTypeConverter
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity
import java.math.BigDecimal

@TypeConverters(BigDecimalTypeConverter::class)
data class ChartDataDto(
    @ColumnInfo(name = "categoryId")
    val categoryId: Int,
    @ColumnInfo(name = "sumOfMoney")
    val sumOfMoney: BigDecimal,
    @ColumnInfo(name = "categoryName")
    val categoryName: String,
    @ColumnInfo(name = "categoryType")
    val categoryType: Int,
    @ColumnInfo(name = "categoryImageRes")
    val categoryImageResourceName: String,
    @ColumnInfo(name = "categoryImageBackgroundColor")
    val categoryImageBackgroundColor: String,
) {

    val isExpensesCategory: Boolean
        get() = categoryType == CategoryEntity.EXPENSES_TYPE_MARKER

    val isIncomeCategory: Boolean
        get() = categoryType == CategoryEntity.INCOME_TYPE_MARKER

    fun toChartData(
        percentage: Float
    ): ChartData = ChartData(
        categoryId = this.categoryId,
        sumOfMoney = this.sumOfMoney,
        categoryName = this.categoryName,
        categoryType = this.categoryType,
        categoryImage = Image(
            resourceName = this.categoryImageResourceName,
            backgroundColor = this.categoryImageBackgroundColor
        ),
        percentage = percentage,
    )
}