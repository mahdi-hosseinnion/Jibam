package com.ssmmhh.jibam.persistence.entities

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage
import com.ssmmhh.jibam.util.getResourcesStringValueByName

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    val id: Int,
    /**
     * 1 => expenses
     * 2 => income
     */
    @ColumnInfo(name = "type")
    val type: Int,
    @ColumnInfo(name = "category_Name")
    val name: String,
    @ColumnInfo(name = "img_res")
    val img_res: String,
    @ColumnInfo(name = "ordering")
    val ordering: Int
) {


    fun getCategoryNameFromStringFile(
        context: Context,
        defaultName: String = name
    ): String = getResourcesStringValueByName(context, this.name) ?: defaultName

    fun getCategoryImageResourceId(
        context: Context,
    ): Int = getCategoryImageResourceIdFromDrawableByCategoryImage(context, this.img_res)

    val isExpensesCategory: Boolean
        get() = type == EXPENSES_TYPE_MARKER

    val isIncomeCategory: Boolean
        get() = type == INCOME_TYPE_MARKER

    companion object {
        const val EXPENSES_TYPE_MARKER = 1
        const val INCOME_TYPE_MARKER = 2
        const val COLUMN_ID = "cId"
    }
}