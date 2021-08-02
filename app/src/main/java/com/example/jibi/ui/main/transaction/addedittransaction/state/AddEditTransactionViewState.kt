package com.example.jibi.ui.main.transaction.addedittransaction.state

import com.example.jibi.models.Category
import com.example.jibi.models.Transaction
import com.example.jibi.util.StateEvent

data class AddEditTransactionViewState(

    val transaction: Transaction? = null,
    val categoriesList: List<Category>? = null,
    val insertedTransactionId: Int? = null,
    val deletedTransactionId: Int? = null,
)
