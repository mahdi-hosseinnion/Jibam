package com.ssmmhh.jibam.ui.main.transaction.transactions.state

import com.ssmmhh.jibam.models.Month
import com.ssmmhh.jibam.persistence.dtos.TransactionDto
import com.ssmmhh.jibam.models.TransactionsRecyclerViewItem

data class TransactionsViewState(
    var recentlyDeletedFields: RecentlyDeletedTransaction? = null,
    var insertedTransactionRawId: Long? = null,
    var successfullyDeletedTransactionIndicator: Int? = null,
    var searchViewState: SearchViewState? = null,
    var currentMonth: Month? = null,
    var calendarType:String? =null
) {



    data class RecentlyDeletedTransaction(
        var recentlyDeletedTrans: TransactionDto,
        var recentlyDeletedTransPosition: Int,
        var recentlyDeletedHeader: TransactionsRecyclerViewItem.Header?
    )

    data class TransactionsQueryRequirement(
        val query: String,
        val month: Month
    )

    enum class SearchViewState {
        VISIBLE, INVISIBLE
    }

}