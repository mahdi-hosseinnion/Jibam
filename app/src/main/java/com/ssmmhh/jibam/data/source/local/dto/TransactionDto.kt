package com.ssmmhh.jibam.data.source.local.dto

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.ssmmhh.jibam.data.model.Image
import com.ssmmhh.jibam.data.model.TransactionsRecyclerViewItem
import com.ssmmhh.jibam.data.source.local.typeconverter.BigDecimalTypeConverter
import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage
import com.ssmmhh.jibam.util.getResourcesStringValueByName
import java.math.BigDecimal
@TypeConverters(BigDecimalTypeConverter::class)
data class TransactionDto(
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "money")
    val money: BigDecimal,
    @ColumnInfo(name = "memo")
    val memo: String?,
    @ColumnInfo(name = "categoryId")
    val categoryId: Int,
    @ColumnInfo(name = "categoryName")
    val categoryName: String,
    @ColumnInfo(name = "categoryImage")
    val categoryImageResourceName: String,
    @ColumnInfo(name = "categoryImageColor")
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