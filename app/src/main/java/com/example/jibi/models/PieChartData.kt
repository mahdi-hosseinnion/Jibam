package com.example.jibi.models

import android.content.res.Resources
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class PieChartData(
    @ColumnInfo(name = "categoryId")
    val categoryId: Int,
    @ColumnInfo(name = "percentage")
    val percentage: Double? = null,
    @ColumnInfo(name = "sumOfMoney")
    val sumOfMoney: Double,
    @ColumnInfo(name = "categoryName")
    val categoryName: String,
    @ColumnInfo(name = "categoryType")
    val categoryType: Int,
    @ColumnInfo(name = "categoryImage")
    val categoryImage: String,
) {
    fun getCategoryNameFromStringFile(
        resources: Resources,
        packageName: String,
        onUnableToFindName: (PieChartData) -> String
    ): String {
        val nameId: Int = resources.getIdentifier(
            this.categoryName,
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
            Log.e(
                "Category",
                "getCategoryNameFromStringFile: add >${this.categoryName}< to strings file"
            )
            onUnableToFindName(this)
        }
    }
}