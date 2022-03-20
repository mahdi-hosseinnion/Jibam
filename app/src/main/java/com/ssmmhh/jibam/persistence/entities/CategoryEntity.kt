package com.ssmmhh.jibam.persistence.entities

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ssmmhh.jibam.util.getResourcesStringValueByName

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = CategoryImageEntity::class,
            parentColumns = arrayOf(CategoryImageEntity.COLUMN_ID),
            childColumns = arrayOf(CategoryEntity.COLUMN_CATEGORY_IMAGE_ID),
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
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
    @ColumnInfo(name = COLUMN_CATEGORY_IMAGE_ID)
    val categoryImageId: Int,
    @ColumnInfo(name = "ordering")
    val ordering: Int
) {


    fun getCategoryNameFromStringFile(
        context: Context,
        defaultName: String = name
    ): String = getResourcesStringValueByName(context, this.name) ?: defaultName


    val isExpensesCategory: Boolean
        get() = type == EXPENSES_TYPE_MARKER

    val isIncomeCategory: Boolean
        get() = type == INCOME_TYPE_MARKER

    companion object {
        const val EXPENSES_TYPE_MARKER = 1
        const val INCOME_TYPE_MARKER = 2
        const val COLUMN_ID = "cId"
        const val COLUMN_CATEGORY_IMAGE_ID = "imageId"
    }
}