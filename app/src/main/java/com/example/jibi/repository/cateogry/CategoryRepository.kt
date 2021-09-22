package com.example.jibi.repository.cateogry

import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
import com.example.jibi.ui.main.transaction.categories.addcategoires.state.AddCategoryStateEvent
import com.example.jibi.ui.main.transaction.categories.addcategoires.state.AddCategoryViewState
import com.example.jibi.ui.main.transaction.categories.viewcategories.state.ViewCategoriesStateEvent
import com.example.jibi.ui.main.transaction.categories.viewcategories.state.ViewCategoriesViewState
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
        stateEvent: ViewCategoriesStateEvent.GetAllOfCategories
    ): DataState<ViewCategoriesViewState>

    suspend fun getAllOfCategories(
        stateEvent: InsertTransactionStateEvent.GetAllOfCategories
    ): DataState<InsertTransactionViewState>

    suspend fun insertCategory(
        stateEvent: AddCategoryStateEvent.InsertCategory
    ): DataState<AddCategoryViewState>

    suspend fun deleteCategory(
        stateEvent: ViewCategoriesStateEvent.DeleteCategory
    ): DataState<ViewCategoriesViewState>

    suspend fun changeCategoryOrder(
        stateEvent: ViewCategoriesStateEvent.ChangeCategoryOrder
    ): DataState<ViewCategoriesViewState>


}