package com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state

import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.util.Event
import java.util.*

data class InsertTransactionViewState(
    val categoryEntity: CategoryEntity? = null,
    val moneyStr: String? = null,
    val finalMoney: Double? = null,
    val memo: String? = null,
    val combineCalender: GregorianCalendar? = null,
    val allOfCategories: Event<List<CategoryEntity>?>? = null,
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
