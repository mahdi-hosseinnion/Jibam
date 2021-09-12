package com.example.jibi.ui.main.transaction.addedittransaction.state

import com.example.jibi.models.Category
import com.example.jibi.models.Transaction
import com.example.jibi.util.StateEvent

data class AddEditTransactionViewState(

    val transaction: Transaction? = null,
    var transactionCategory: Category? = null,
    val categoriesList: List<Category>? = null,
    val insertedTransactionRawId: Long? = null,
    val successfullyDeletedTransactionIndicator: Int? = null,
    val presenterState: PresenterState? = null
)

sealed class PresenterState() {

    data class SelectingCategory(
        val default_category_id: Int
    ) : PresenterState()

    object EnteringAmountOfMoney : PresenterState()

    object NormalState : PresenterState()

}

