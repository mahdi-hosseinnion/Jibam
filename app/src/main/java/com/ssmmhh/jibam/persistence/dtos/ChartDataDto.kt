package com.ssmmhh.jibam.persistence.dtos

import androidx.room.ColumnInfo
import com.ssmmhh.jibam.models.CategoryImage
import com.ssmmhh.jibam.models.ChartData
import com.ssmmhh.jibam.persistence.entities.CategoryEntity

data class ChartDataDto(
    @ColumnInfo(name = "categoryId")
    val categoryId: Int,
    @ColumnInfo(name = "sumOfMoney")
    val sumOfMoney: Double,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "categoryType")
    val categoryType: Int,
    @ColumnInfo(name = "category_image_res")
    val categoryImageResourceName: String,
    @ColumnInfo(name = "category_image_background_color")
    val categoryImageBackgroundColor: String,
) {

    val isExpensesCategory: Boolean
        get() = categoryType == CategoryEntity.EXPENSES_TYPE_MARKER

    val isIncomeCategory: Boolean
        get() = categoryType == CategoryEntity.INCOME_TYPE_MARKER

    fun toChartData(
        percentage: Double
    ): ChartData = ChartData(
        categoryId = this.categoryId,
        sumOfMoney = this.sumOfMoney,
        categoryName = this.categoryName,
        categoryType = this.categoryType,
        categoryImage = CategoryImage(
            resourceName = this.categoryImageResourceName,
            backgroundColor = this.categoryImageBackgroundColor
        ),
        percentage = percentage,
    )
}