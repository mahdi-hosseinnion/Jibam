package com.ssmmhh.jibam.models

import android.content.Context
import androidx.room.ColumnInfo
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.util.getResourcesStringValueByName

data class Category(
    val id: Int,
    val type: Int,
    val name: String,
    val ordering: Int,
    val image: CategoryImage
){
    fun getCategoryNameFromStringFile(
        context: Context,
        defaultName: String = name
    ): String = getResourcesStringValueByName(context, this.name) ?: defaultName

    val isExpensesCategory: Boolean
        get() = type == CategoryEntity.EXPENSES_TYPE_MARKER

    val isIncomeCategory: Boolean
        get() = type == CategoryEntity.INCOME_TYPE_MARKER
}