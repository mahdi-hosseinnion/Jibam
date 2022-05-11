package com.ssmmhh.jibam.data.model

import android.content.Context
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage
import com.ssmmhh.jibam.util.getResourcesStringValueByName
import java.math.BigDecimal


data class ChartData(
    val categoryId: Int,
    val sumOfMoney: BigDecimal,
    val categoryName: String,
    val categoryType: Int,
    val categoryImage: Image,
    val percentage: Float
) {
    /**
     * Databinding does not support kotlin's default function arguments.
     */
    fun getCategoryNameFromStringFile(
        context: Context,
    ): String = getCategoryNameFromStringFile(context, this.categoryName)

    fun getCategoryNameFromStringFile(
        context: Context,
        defaultName: String = this.categoryName
    ): String = getResourcesStringValueByName(context, this.categoryName) ?: defaultName

    fun getCategoryImageResourceId(
        context: Context,
    ): Int = getCategoryImageResourceIdFromDrawableByCategoryImage(
        context,
        this.categoryImage.resourceName
    )

    val isExpensesCategory: Boolean
        get() = categoryType == CategoryEntity.EXPENSES_TYPE_MARKER

    val isIncomeCategory: Boolean
        get() = categoryType == CategoryEntity.INCOME_TYPE_MARKER
}
