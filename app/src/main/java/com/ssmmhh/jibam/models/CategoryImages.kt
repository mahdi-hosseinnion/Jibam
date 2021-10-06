package com.ssmmhh.jibam.models

import android.content.res.Resources
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "category_images")
data class CategoryImages(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "image_res")
    val image_res: String,
    @ColumnInfo(name = "group_name")
    val group_name: String
){

    fun getCategoryGroupNameFromStringFile(
        resources: Resources,
        packageName: String,
        onUnableToFindName: (CategoryImages) -> String
    ): String {
        val nameId: Int = resources.getIdentifier(
            this.group_name,
            "string",
            packageName
        )
        return try {
            resources.getString(nameId)
        } catch (e: Exception) {
            Log.e(
                "Category",
                "getCategoryGroupNameFromStringFile: UNABLE TO FIND $this name in strings ",
            )
            Log.e("Category", "getCategoryGroupNameFromStringFile: add >${this.group_name}< to strings file")
            onUnableToFindName(this)
        }
    }
}