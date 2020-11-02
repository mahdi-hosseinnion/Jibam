package com.example.jibi.ui.main.transaction.state

import com.example.jibi.models.Record
import com.example.jibi.models.SummaryMoney

data class TransactionViewState(

    //Last Transaction vars
    var transactionList: List<Record>? = null,
    //summery money
    var summeryMoney: SummaryMoney? = null

) {

}