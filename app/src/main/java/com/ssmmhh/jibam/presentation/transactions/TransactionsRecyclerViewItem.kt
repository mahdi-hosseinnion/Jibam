package com.ssmmhh.jibam.presentation.transactions

import com.ssmmhh.jibam.data.model.Image
import com.ssmmhh.jibam.data.model.Transaction
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import java.math.BigDecimal

sealed class TransactionsRecyclerViewItem(
    val itemType: Int
) {

    data class TransactionItem(
        val transaction: Transaction
    ) : TransactionsRecyclerViewItem(TRANSACTION_VIEW_TYPE)


    data class Header(
        val date: Long,
        val expensesSum: BigDecimal?,
        val incomeSum: BigDecimal?
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

val TransactionsRecyclerViewItem.isTransaction get() = this is TransactionsRecyclerViewItem.TransactionItem

val TransactionsRecyclerViewItem.isHeader get() = this is TransactionsRecyclerViewItem.Header

fun TransactionsRecyclerViewItem.TransactionItem.toTransaction(): TransactionDto =
    transaction.toTransactionDto()

fun TransactionDto.toTransactionsRecyclerViewItem(): TransactionsRecyclerViewItem.TransactionItem =
    TransactionsRecyclerViewItem.TransactionItem(
        transaction = this.toTransaction()
    )





