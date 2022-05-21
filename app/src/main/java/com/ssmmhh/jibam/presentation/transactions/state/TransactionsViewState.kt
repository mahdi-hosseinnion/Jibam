package com.ssmmhh.jibam.presentation.transactions.state

import com.ssmmhh.jibam.data.model.Month

data class TransactionsViewState(
    var insertedTransactionRawId: Long? = null,
    var successfullyDeletedTransactionIndicator: Int? = null,
    var currentMonth: Month? = null,
) {

    data class TransactionsQueryRequirement(
        val query: String,
        val month: Month
    )

    enum class SearchViewState {
        VISIBLE, INVISIBLE
    }

}