package com.example.jibi.ui.main.transaction.transactions.state

import com.example.jibi.models.Month
import com.example.jibi.models.Transaction
import com.example.jibi.ui.main.transaction.transactions.TransactionsViewModel

data class TransactionsViewState(
    var recentlyDeletedFields: RecentlyDeletedTransaction? = null,
    var insertedTransactionRawId: Long? = null,
    var successfullyDeletedTransactionIndicator: Int? = null,
    var searchViewState: SearchViewState? = null
) {

    data class SummaryMoney(
        var balance: Double = 0.0,
        var income: Double = 0.0,
        var expenses: Double = 0.0
    ) {
        fun inNotNull(): Boolean {
            return (balance != 0.0 ||
                    income != 0.0 ||
                    expenses != 0.0)
        }
    }

    data class RecentlyDeletedTransaction(
        var recentlyDeletedTrans: Transaction,
        var recentlyDeletedTransPosition: Int,
        var recentlyDeletedHeader: Transaction?
    )

    data class TransactionsQueryRequirement(
        val query: String,
        val month: Month
    )

    enum class SearchViewState {
        VISIBLE,INVISIBLE
    }

}