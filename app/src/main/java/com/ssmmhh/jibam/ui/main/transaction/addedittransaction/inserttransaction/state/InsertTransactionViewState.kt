package com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state

import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.util.Event
import java.util.*

data class InsertTransactionViewState(
    val category: Category? = null,
    val moneyStr: String? = null,
    val finalMoney: Double? = null,
    val memo: String? = null,
    val combineCalender: GregorianCalendar? = null,
    val allOfCategories: Event<List<Category>?>? = null,
    val insertedTransactionRawId: Long? = null,
    val presenterState: Event<InsertTransactionPresenterState>? = null

)

sealed class InsertTransactionPresenterState() {

    object SelectingCategoryState : InsertTransactionPresenterState()

    object EnteringAmountOfMoneyState : InsertTransactionPresenterState()

    object ChangingDateState : InsertTransactionPresenterState()

    object ChangingTimeState : InsertTransactionPresenterState()

    object AddingNoteState : InsertTransactionPresenterState()

    object NoneState : InsertTransactionPresenterState()

}
