package com.ssmmhh.jibam.persistence.entities

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage
import com.ssmmhh.jibam.util.getResourcesStringValueByName


@Entity(tableName = "category_images")
data class CategoryImageEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "image_res")
    val image_res: String,
    @ColumnInfo(name = "group_name")
    val group_name: String
) {

    fun getCategoryGroupNameFromStringFile(
        context: Context,
        defaultName: String = group_name
    ): String = getResourcesStringValueByName(context, this.group_name) ?: defaultName

    fun getCategoryImageResourceId(
        context: Context,
    ): Int = getCategoryImageResourceIdFromDrawableByCategoryImage(context, image_res)
}