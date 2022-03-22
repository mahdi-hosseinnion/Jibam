package com.ssmmhh.jibam.persistence.dtos

import android.content.Context
import android.content.res.Resources
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage
import com.ssmmhh.jibam.util.getResourcesStringValueByName

data class ChartDataDto(
    @ColumnInfo(name = "categoryId")
    val categoryId: Int,
    @ColumnInfo(name = "sumOfMoney")
    val sumOfMoney: Double,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "categoryType")
    val categoryType: Int,
    @ColumnInfo(name = "category_image")
    val categoryImageResourceName: String,
    @ColumnInfo(name = "category_image_background_color")
    val categoryImageBackgroundColor: String,
) {

    val isExpensesCategory: Boolean
        get() = categoryType == CategoryEntity.EXPENSES_TYPE_MARKER

    val isIncomeCategory: Boolean
        get() = categoryType == CategoryEntity.INCOME_TYPE_MARKER
}