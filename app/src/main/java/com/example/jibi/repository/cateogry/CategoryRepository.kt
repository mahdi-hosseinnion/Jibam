package com.example.jibi.repository.cateogry

import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionViewState
import com.example.jibi.ui.main.transaction.categories.state.CategoriesStateEvent
import com.example.jibi.ui.main.transaction.categories.state.CategoriesViewState
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

    suspend fun insertCategory(
        stateEvent: CategoriesStateEvent.InsertCategory
    ): DataState<CategoriesViewState>

    suspend fun deleteCategory(
        stateEvent: CategoriesStateEvent.DeleteCategory
    ): DataState<CategoriesViewState>

    suspend fun changeCategoryOrder(
        stateEvent: CategoriesStateEvent.ChangeCategoryOrder
    ): DataState<CategoriesViewState>

    suspend fun changeCategoryOrderNew(
        stateEvent: CategoriesStateEvent.ChangeCategoryOrderNew
    ): DataState<CategoriesViewState>


}