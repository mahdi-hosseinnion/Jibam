package com.ssmmhh.jibam.repository.cateogry

import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.models.CategoryImages
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires.state.AddCategoryStateEvent
import com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires.state.AddCategoryViewState
import com.ssmmhh.jibam.ui.main.transaction.categories.viewcategories.state.ViewCategoriesStateEvent
import com.ssmmhh.jibam.ui.main.transaction.categories.viewcategories.state.ViewCategoriesViewState
import com.ssmmhh.jibam.util.DataState
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