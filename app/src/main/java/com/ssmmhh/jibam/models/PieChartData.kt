package com.ssmmhh.jibam.models

import android.content.res.Resources
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Ignore

data class PieChartData(
    @ColumnInfo(name = "categoryId")
    val categoryId: Int,
    @ColumnInfo(name = "sumOfMoney")
    val sumOfMoney: Double,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "categoryType")
    val categoryType: Int,
    @ColumnInfo(name = "category_image")
    val categoryImage: String,
    @Ignore val percentage: Double

) {

    constructor(categoryId: Int, sumOfMoney: Double, categoryName: String,categoryType: Int,categoryImage: String)
            : this(categoryId,sumOfMoney,categoryName,categoryType,categoryImage,0.0)

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