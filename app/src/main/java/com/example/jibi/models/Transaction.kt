package com.example.jibi.models

import androidx.room.ColumnInfo
import android.content.res.Resources
import android.util.Log
import androidx.room.Ignore

data class Transaction(
    @ColumnInfo(name = "rId")
    val id: Int,
    @ColumnInfo(name = "money")
    val money: Double,
    @ColumnInfo(name = "memo")
    val memo: String?,
    @ColumnInfo(name = "cat_id")
    //category id exactly id
    val categoryId: Int,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "category_image")
    val categoryImage: String,

    //int can handle the time till 1/19/2038, 6:44:07 AM in millisecond
    @ColumnInfo(name = "date")
    val date: Int,
    //handle the income for repository
    @Ignore val incomeSum: Double?
) {
    constructor(
//for transaction show in transaction list
        id: Int,
        money: Double,
        memo: String?,
        categoryId: Int,
        categoryName: String,
        categoryImage: String,
        date: Int,
    ) : this(id, money, memo, categoryId, categoryName, categoryImage, date, null)

    constructor(//for header
        id: Int,
        money: Double,
        memo: String,
        incomeSum: Double
    ) : this(id, money, memo, 0, "", "", 0, incomeSum)

    fun getCategoryNameFromStringFile(
        resources: Resources,
        packageName: String,
        onUnableToFindName: (Transaction) -> String
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