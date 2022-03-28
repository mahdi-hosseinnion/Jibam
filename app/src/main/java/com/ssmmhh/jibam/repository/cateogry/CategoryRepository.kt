package com.ssmmhh.jibam.repository.cateogry

import com.ssmmhh.jibam.persistence.dtos.CategoryDto
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.persistence.entities.CategoryImageEntity
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires.state.AddCategoryStateEvent
import com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires.state.AddCategoryViewState
import com.ssmmhh.jibam.ui.main.transaction.categories.viewcategories.state.ViewCategoriesStateEvent
import com.ssmmhh.jibam.ui.main.transaction.categories.viewcategories.state.ViewCategoriesViewState
import com.ssmmhh.jibam.ui.main.transaction.common.state.GetAllOfCategoriesViewState
import com.ssmmhh.jibam.util.DataState
import com.ssmmhh.jibam.util.StateEvent
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategoryList(
    ): Flow<List<CategoryDto>>

    fun getCategoryImages(
    ): Flow<List<CategoryImageEntity>>

    suspend fun getAllOfCategories(
        stateEvent: StateEvent
    ): DataState<GetAllOfCategoriesViewState>

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