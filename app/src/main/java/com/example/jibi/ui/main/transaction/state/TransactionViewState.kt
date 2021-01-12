package com.example.jibi.ui.main.transaction.state

import android.icu.text.Collator
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.models.SummaryMoney

data class TransactionViewState(

    //Last Transaction vars
    var transactionList: List<Record>? = null,
    //List of all categories
    var categoryList: List<Category>? = null,
    //summery money
    var summeryMoney: SummaryMoney? = null,

    //detail fragment
    var detailTransFields: Record? = null,

    //swipe to delete
    var recentlyDeletedFields: RecentlyDeletedFields = RecentlyDeletedFields()
) {
    data class RecentlyDeletedFields(
        var recentlyDeletedTrans: Record? = null,
        var recentlyDeletedTransPosition: Int? = null,
        var recentlyDeletedHeader: Record? = null
    )
}