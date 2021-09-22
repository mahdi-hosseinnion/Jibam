package com.example.jibi.ui.main.transaction.categories.viewcategories

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.jibi.models.CategoryImages
import com.example.jibi.repository.cateogry.CategoryRepository
import com.example.jibi.ui.main.transaction.categories.viewcategories.state.ViewCategoriesStateEvent
import com.example.jibi.ui.main.transaction.categories.viewcategories.state.ViewCategoriesViewState
import com.example.jibi.ui.main.transaction.common.BaseViewModel
import com.example.jibi.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
//@MainScope
class ViewCategoriesViewModel
@Inject
constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel<ViewCategoriesViewState, ViewCategoriesStateEvent>() {

    init {
        refreshCategoryList()
    }

    private val _categoriesImages: LiveData<List<CategoryImages>> =
        categoryRepository.getCategoryImages()
            .asLiveData()

    val categoriesImages: LiveData<List<CategoryImages>> = _categoriesImages


    override fun initNewViewState(): ViewCategoriesViewState = ViewCategoriesViewState()

    override suspend fun getResultByStateEvent(stateEvent: ViewCategoriesStateEvent): DataState<ViewCategoriesViewState> =
        when (stateEvent) {
            is ViewCategoriesStateEvent.InsertCategory -> categoryRepository.insertCategory(
                stateEvent
            )
            is ViewCategoriesStateEvent.DeleteCategory -> categoryRepository.deleteCategory(
                stateEvent
            )
            is ViewCategoriesStateEvent.ChangeCategoryOrder -> categoryRepository.changeCategoryOrder(
                stateEvent
            )
            is ViewCategoriesStateEvent.GetAllOfCategories -> categoryRepository.getAllOfCategories(
                stateEvent
            )
        }

    override fun updateViewState(newViewStateView: ViewCategoriesViewState): ViewCategoriesViewState {
        val outDate = getCurrentViewStateOrNew()
        return ViewCategoriesViewState(
            newViewStateView.categoryList ?: outDate.categoryList,
            newViewStateView.insertedCategoryRow ?: outDate.insertedCategoryRow
        )
    }

    fun refreshCategoryList() {
        launchNewJob(
            ViewCategoriesStateEvent.GetAllOfCategories
        )
    }

    fun newReorder(newOrder: HashMap<Int, Int>, type: Int) {
        launchNewJob(
            ViewCategoriesStateEvent.ChangeCategoryOrder(
                newOrder,
                type
            )
        )
    }


}