package com.example.jibi.ui.main.transaction.state

import com.example.jibi.models.Category
import com.example.jibi.models.PieChartData
import com.example.jibi.models.TransactionEntity

data class TransactionViewState12(

    //Last Transaction vars
    var transactionList: List<TransactionEntity>? = null,
    //List of all categories
    var categoryList: List<Category>? = null,
    //summery money
//    var summeryMoney: SummaryMoney = SummaryMoney(),

    //detail fragment
    var detailTransFields: TransactionEntity? = null,
    //chart fragment
    var pieChartData: List<PieChartData>? = null,
    //detail chart
    var detailChartFields: DetailChartFields = DetailChartFields(),
    //swipe to delete
//    var recentlyDeletedFields: RecentlyDeletedFields = RecentlyDeletedFields()
) {

/*    data class SummaryMoney(
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

    data class RecentlyDeletedFields(
        var recentlyDeletedTrans: Record? = null,
        var recentlyDeletedTransPosition: Int? = null,
        var recentlyDeletedHeader: Record? = null
    )*/

/*    data class DetailChartFields(
        var category: Category? = null,
        var allTransaction: List<TransactionEntity>? = null
    )*/
}