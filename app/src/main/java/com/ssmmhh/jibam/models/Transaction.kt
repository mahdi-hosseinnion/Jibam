package com.ssmmhh.jibam.models

import androidx.room.ColumnInfo
import android.content.res.Resources
import android.util.Log
import androidx.room.Ignore
import com.ssmmhh.jibam.persistence.entities.TransactionEntity

data class Transaction(
    @ColumnInfo(name = "rId")
    val id: Int,
    @ColumnInfo(name = "money")
    val money: Double,
    @ColumnInfo(name = "memo")
    val memo: String?,
    @ColumnInfo(name = "cat_id")
    val categoryId: Int,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "category_image")
    val categoryImage: String,
    //int can handle the time till 1/19/2038, 6:44:07 AM in millisecond
    @ColumnInfo(name = "date")
    val date: Int,
) {

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

    fun toTransactionEntity(): TransactionEntity = TransactionEntity(
        id = this.id,
        money = this.money,
        memo = this.memo,
        cat_id = this.categoryId,
        date = this.date
    )

    fun toTransactionsRecyclerViewItem(): TransactionsRecyclerViewItem.Transaction =
        TransactionsRecyclerViewItem.Transaction(
            id = this.id,
            money = this.money,
            memo = this.memo,
            categoryId = this.categoryId,
            categoryName = this.categoryName,
            categoryImage = this.categoryImage,
            date = this.date
        )
}