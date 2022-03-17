package com.ssmmhh.jibam.models

import android.content.Context
import android.content.res.Resources
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage
import com.ssmmhh.jibam.util.getResourcesStringValueByName

data class PieChartData(
    @ColumnInfo(name = "categoryId")
    val categoryId: Int,
    @ColumnInfo(name = "sumOfMoney")
    val sumOfMoney: Double,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "categoryType")
    val categoryType: Int,
    @ColumnInfo(name = "category_image")
    val categoryImage: String,
    @Ignore val percentage: Double

) {

    constructor(
        categoryId: Int,
        sumOfMoney: Double,
        categoryName: String,
        categoryType: Int,
        categoryImage: String
    ) : this(categoryId, sumOfMoney, categoryName, categoryType, categoryImage, 0.0)

    fun getCategoryNameFromStringFile(
        context: Context,
        defaultName: String = this.categoryName
    ): String =
        getResourcesStringValueByName(context, this.categoryName) ?: defaultName

    fun getCategoryImageResourceId(
        context: Context,
    ): Int = getCategoryImageResourceIdFromDrawableByCategoryImage(context, this.categoryImage)

    val isExpensesCategory: Boolean
        get() = categoryType == CategoryEntity.EXPENSES_TYPE_MARKER

    val isIncomeCategory: Boolean
        get() = categoryType == CategoryEntity.INCOME_TYPE_MARKER
}