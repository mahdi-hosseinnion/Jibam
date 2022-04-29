package com.ssmmhh.jibam.presentation.transactions.state

import com.ssmmhh.jibam.data.model.Month
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.presentation.transactions.TransactionsRecyclerViewItem

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