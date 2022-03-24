package com.ssmmhh.jibam.persistence.dtos

import android.content.Context
import androidx.room.ColumnInfo
import com.ssmmhh.jibam.models.Image
import com.ssmmhh.jibam.models.TransactionsRecyclerViewItem
import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage
import com.ssmmhh.jibam.util.getResourcesStringValueByName
import java.math.BigDecimal

data class TransactionDto(
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "money")
    val money: BigDecimal,
    @ColumnInfo(name = "memo")
    val memo: String?,
    @ColumnInfo(name = "categoryId")
    val categoryId: Int,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "category_image")
    val categoryImageResourceName: String,
    @ColumnInfo(name = "category_image_color")
    val categoryImageBackgroundColor: String,
    @ColumnInfo(name = "date")
    val date: Long,
) {

    fun getCategoryNameFromStringFile(
        context: Context,
        defaultName: String = this.categoryName
    ): String =
        getResourcesStringValueByName(context, this.categoryName) ?: defaultName

    fun getCategoryImageResourceId(
        context: Context,
    ): Int = getCategoryImageResourceIdFromDrawableByCategoryImage(
        context,
        this.categoryImageResourceName
    )

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
            image = Image(
                resourceName = this.categoryImageResourceName,
                backgroundColor = this.categoryImageBackgroundColor,
            ),
            date = this.date
        )
}