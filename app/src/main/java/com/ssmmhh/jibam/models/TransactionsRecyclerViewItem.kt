package com.ssmmhh.jibam.models

import android.content.Context
import com.ssmmhh.jibam.persistence.dtos.TransactionDto
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage
import com.ssmmhh.jibam.util.getResourcesStringValueByName

sealed class TransactionsRecyclerViewItem(
    val itemType: Int
) {

    data class Transaction(
        val id: Int,
        val money: Double,
        val memo: String?,
        val categoryId: Int,
        val categoryName: String,
        val categoryImage: String,
        val date: Int,
    ) : TransactionsRecyclerViewItem(TRANSACTION_VIEW_TYPE)


    data class Header(
        val date: String,
        val expensesSum: Double?,
        val incomeSum: Double?
    ) : TransactionsRecyclerViewItem(HEADER_VIEW_TYPE)


    object NoMoreResult : TransactionsRecyclerViewItem(NO_MORE_RESULT_VIEW_TYPE)
    object NoResultFound : TransactionsRecyclerViewItem(NO_RESULT_FOUND_VIEW_TYPE)
    object DatabaseIsEmpty : TransactionsRecyclerViewItem(DATABASE_IS_EMPTY_VIEW_TYPE)

    companion object {
        //ViewTypes
        const val TRANSACTION_VIEW_TYPE = 1
        const val HEADER_VIEW_TYPE = 2
        const val NO_MORE_RESULT_VIEW_TYPE = 3
        const val NO_RESULT_FOUND_VIEW_TYPE = 4
        const val DATABASE_IS_EMPTY_VIEW_TYPE = 5
    }

}

val TransactionsRecyclerViewItem.isTransaction get() = this is TransactionsRecyclerViewItem.Transaction

val TransactionsRecyclerViewItem.isHeader get() = this is TransactionsRecyclerViewItem.Header

fun TransactionsRecyclerViewItem.Transaction.getCategoryNameFromStringFile(
    context: Context,
    defaultName: String = categoryName
): String = getResourcesStringValueByName(context, this.categoryName) ?: defaultName

fun TransactionsRecyclerViewItem.Transaction.getCategoryImageResourceId(
    context: Context,
): Int = getCategoryImageResourceIdFromDrawableByCategoryImage(context, this.categoryImage)

fun TransactionsRecyclerViewItem.Transaction.toTransaction(): TransactionDto = TransactionDto(
    id = this.id,
    money = this.money,
    memo = this.memo,
    categoryId = this.categoryId,
    categoryName = this.categoryName,
    categoryImage = this.categoryImage,
    date = this.date,
)




