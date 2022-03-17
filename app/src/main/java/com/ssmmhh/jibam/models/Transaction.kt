package com.ssmmhh.jibam.models

import android.content.Context
import androidx.room.ColumnInfo
import android.content.res.Resources
import android.util.Log
import androidx.room.Ignore
import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage
import com.ssmmhh.jibam.util.getResourcesStringValueByName

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
        context: Context,
        defaultName: String = this.categoryName
    ): String =
        getResourcesStringValueByName(context, this.categoryName) ?: defaultName

    fun getCategoryImageResourceId(
        context: Context,
    ): Int = getCategoryImageResourceIdFromDrawableByCategoryImage(context, this.categoryImage)

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