package com.example.jibi.models

import android.content.res.Resources
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "cId")
    val id: Int,
    /*
    1 => expenses
    2 => income
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
        resources: Resources,
        packageName: String,
        onUnableToFindName: (Category) -> String
    ): String {
        val nameId: Int = resources.getIdentifier(
            this.name,
            "string",
            packageName
        )
        return try {
            resources.getString(nameId)
        } catch (e: Exception) {
            Log.e(
                "Category",
                "getCategoryNameFromStringFile: UNABLE TO FIND $this name in strings ",
            )
            Log.e("Category", "getCategoryNameFromStringFile: add >${this.name}< to strings file")
            onUnableToFindName(this)
        }
    }
}