package com.example.jibi.ui.main.transaction.state

import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.models.SummaryMoney

data class TransactionViewState(

    //Last Transaction vars
    var transactionList: List<Record>? = null,
    //List of all categories
    var categoryList: List<Category>? = null,
    //summery money
    var summeryMoney: SummaryMoney? = null

) {

}