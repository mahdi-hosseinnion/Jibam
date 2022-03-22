package com.ssmmhh.jibam.persistence.dtos

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.models.CategoryImage
import com.ssmmhh.jibam.persistence.entities.CategoryEntity

/**
 * Used as model for database queries
 */
data class CategoryDto(
    @ColumnInfo(name = "cId")
    val id: Int,
    @ColumnInfo(name = "type")
    val type: Int,
    @ColumnInfo(name = "category_Name")
    val name: String,
    @ColumnInfo(name = "ordering")
    val ordering: Int,
    @ColumnInfo(name = "imageResourceId")
    val imageResourceId: String,
    @ColumnInfo(name = "imageBackgroundColor")
    val imageBackgroundColor: String,
) {

    fun toCategory(): Category = Category(
        id = this.id,
        type = this.type,
        name = this.name,
        ordering = this.ordering,
        image = CategoryImage(
            resourceName = this.imageResourceId,
            backgroundColor = this.imageBackgroundColor
        ),
    )
}