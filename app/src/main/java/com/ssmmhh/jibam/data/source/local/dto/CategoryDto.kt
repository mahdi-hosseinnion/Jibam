package com.ssmmhh.jibam.data.source.local.dto

import androidx.room.ColumnInfo
import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.data.model.Image

/**
 * Used as model for database queries
 */
data class CategoryDto(
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "type")
    val type: Int,
    @ColumnInfo(name = "name")
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
        image = Image(
            resourceName = this.imageResourceId,
            backgroundColor = this.imageBackgroundColor
        ),
    )
}