package com.ssmmhh.jibam.data.source.repository.cateogry

import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.data.source.local.dto.CategoryDto
import com.ssmmhh.jibam.data.source.local.entity.CategoryImageEntity
import com.ssmmhh.jibam.ui.main.transaction.feature_categories.addcategoires.state.AddCategoryStateEvent
import com.ssmmhh.jibam.ui.main.transaction.feature_categories.addcategoires.state.AddCategoryViewState
import com.ssmmhh.jibam.ui.main.transaction.feature_categories.viewcategories.state.ViewCategoriesStateEvent
import com.ssmmhh.jibam.ui.main.transaction.feature_categories.viewcategories.state.ViewCategoriesViewState
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.util.Event
import com.ssmmhh.jibam.util.StateEvent
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategoryList(
    ): Flow<List<CategoryDto>>

    fun getCategoryImages(
    ): Flow<List<CategoryImageEntity>>

    suspend fun getAllOfCategories(
        stateEvent: StateEvent
    ): DataState<Event<List<Category>?>?>

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