package com.ssmmhh.jibam.persistence.entities

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage
import com.ssmmhh.jibam.util.getResourcesStringValueByName


@Entity(tableName = "category_images")
data class CategoryImageEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    val id: Int,
    @ColumnInfo(name = "group_name")
    val group_name: String,
    @ColumnInfo(name = "image_res")
    val image_res: String,
    @ColumnInfo(name = "image_background_color")
    val image_background_color: String
) {

    fun getCategoryGroupNameFromStringFile(
        context: Context,
        defaultName: String = group_name
    ): String = getResourcesStringValueByName(context, this.group_name) ?: defaultName

    fun getCategoryImageResourceId(
        context: Context,
    ): Int = getCategoryImageResourceIdFromDrawableByCategoryImage(context, image_res)

    companion object {
        const val COLUMN_ID = "id"
    }
}