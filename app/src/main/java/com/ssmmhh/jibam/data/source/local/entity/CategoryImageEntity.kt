package com.ssmmhh.jibam.data.source.local.entity

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage
import com.ssmmhh.jibam.util.getResourcesStringValueByName


@Entity(tableName = "categoryImages")
data class CategoryImageEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    val id: Int,
    @ColumnInfo(name = "groupName")
    val groupName: String,
    @ColumnInfo(name = "resName")
    val imageResName: String,
    @ColumnInfo(name = "backgroundColor")
    val image_background_color: String
) {

    fun getCategoryGroupNameFromStringFile(
        context: Context,
        defaultName: String = groupName
    ): String = getResourcesStringValueByName(context, this.groupName) ?: defaultName

    fun getCategoryImageResourceId(
        context: Context,
    ): Int = getCategoryImageResourceIdFromDrawableByCategoryImage(context, imageResName)

    companion object {
        const val COLUMN_ID = "id"
    }
}