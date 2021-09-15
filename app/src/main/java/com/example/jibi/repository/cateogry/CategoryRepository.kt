package com.example.jibi.repository.cateogry

import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
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
        stateEvent: DetailEditTransactionStateEvent.GetAllOfCategories
    ): DataState<DetailEditTransactionViewState>

    suspend fun getAllOfCategories(
        stateEvent: CategoriesStateEvent.GetAllOfCategories
    ): DataState<CategoriesViewState>

    suspend fun getAllOfCategories(
        stateEvent: InsertTransactionStateEvent.GetAllOfCategories
    ): DataState<InsertTransactionViewState>

    suspend fun insertCategory(
        stateEvent: CategoriesStateEvent.InsertCategory
    ): DataState<CategoriesViewState>

    suspend fun deleteCategory(
        stateEvent: CategoriesStateEvent.DeleteCategory
    ): DataState<CategoriesViewState>

    suspend fun changeCategoryOrder(
        stateEvent: CategoriesStateEvent.ChangeCategoryOrder
    ): DataState<CategoriesViewState>


}