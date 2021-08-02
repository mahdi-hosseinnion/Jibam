package com.example.jibi.repository.cateogry

import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionViewState
import com.example.jibi.util.DataState
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategoryList(
    ): Flow<List<Category>>

    fun getCategoryImages(
    ): Flow<List<CategoryImages>>

    suspend fun getAllOfCategories(
        stateEvent: AddEditTransactionStateEvent.GetAllOfCategories
    ): DataState<AddEditTransactionViewState>
}