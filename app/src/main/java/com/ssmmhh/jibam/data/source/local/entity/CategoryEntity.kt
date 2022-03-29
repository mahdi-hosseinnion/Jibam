package com.ssmmhh.jibam.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = COLUMN_CATEGORY_IMAGE_ID)
    val imageId: Int,
    @ColumnInfo(name = "ordering")
    val ordering: Int
) {


    companion object {
        const val EXPENSES_TYPE_MARKER = 1
        const val INCOME_TYPE_MARKER = 2
        const val COLUMN_ID = "id"
        const val COLUMN_CATEGORY_IMAGE_ID = "imageId"
    }

}