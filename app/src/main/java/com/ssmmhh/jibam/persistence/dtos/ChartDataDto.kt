package com.ssmmhh.jibam.persistence.dtos

import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.ssmmhh.jibam.models.Image
import com.ssmmhh.jibam.models.ChartData
import com.ssmmhh.jibam.persistence.typeconverters.BigDecimalTypeConverter
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import java.math.BigDecimal

@TypeConverters(BigDecimalTypeConverter::class)
data class ChartDataDto(
    @ColumnInfo(name = "categoryId")
    val categoryId: Int,
    @ColumnInfo(name = "sumOfMoney")
    val sumOfMoney: BigDecimal,
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