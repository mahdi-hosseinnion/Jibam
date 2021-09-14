package com.example.jibi.ui.main.transaction.addedittransaction.state

import com.example.jibi.models.Category
import com.example.jibi.models.Transaction
import com.example.jibi.util.StateEvent

data class AddEditTransactionViewState(

    val transaction: Transaction? = null,
    //we need categoryType for inserting a transaction to determine if that is expenses or income
    var categoryType: Int? = null,
    val categoriesList: List<Category>? = null,
    val insertedTransactionRawId: Long? = null,
    val successfullyDeletedTransactionIndicator: Int? = null,
    val presenterState: PresenterState? = null
)

sealed class PresenterState() {

    object SelectingCategory : PresenterState()

    object EnteringAmountOfMoney : PresenterState()

    object NormalState : PresenterState()

}

