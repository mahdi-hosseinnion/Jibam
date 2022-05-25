package com.ssmmhh.jibam.data.model

import android.content.Context
import androidx.room.ColumnInfo
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.util.getResourcesStringValueByName
import java.math.BigDecimal

//TODO ("Use transaction model in presentation layout instead of TransactionDto")
data class Transaction(
    val id: Int,
    val money: BigDecimal,
    val memo: String?,
    val categoryId: Int,
    val categoryName: String,
    val categoryImage: Image,
    val date: Long,
) {
    /**
     * Databinding does not support kotlin's default function arguments.
     */
    fun getCategoryNameFromStringFile(context: Context): String =
        getCategoryNameFromStringFile(context, this.categoryName)

    fun getCategoryNameFromStringFile(
        context: Context,
        defaultName: String = categoryName
    ): String = getResourcesStringValueByName(context, this.categoryName) ?: defaultName

    fun toTransactionDto(): TransactionDto = TransactionDto(
        id = this.id,
        money = this.money,
        memo = this.memo,
        categoryId = this.categoryId,
        categoryName = this.categoryName,
        categoryImageResourceName = this.categoryImage.resourceName,
        categoryImageBackgroundColor = this.categoryImage.backgroundColor,
        date = this.date,
    )
}