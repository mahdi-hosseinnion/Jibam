package com.ssmmhh.jibam.feature_categories.viewcategories

import com.ssmmhh.jibam.data.source.repository.cateogry.CategoryRepository
import com.ssmmhh.jibam.feature_categories.viewcategories.state.ViewCategoriesStateEvent
import com.ssmmhh.jibam.feature_categories.viewcategories.state.ViewCategoriesViewState
import com.ssmmhh.jibam.feature_common.BaseViewModel
import com.ssmmhh.jibam.data.util.DataState
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

    override fun initNewViewState(): ViewCategoriesViewState = ViewCategoriesViewState()

    override suspend fun getResultByStateEvent(stateEvent: ViewCategoriesStateEvent): DataState<ViewCategoriesViewState> =
        when (stateEvent) {
            is ViewCategoriesStateEvent.DeleteCategory -> categoryRepository.deleteCategory(
                stateEvent
            )
            is ViewCategoriesStateEvent.ChangeCategoryOrder -> categoryRepository.changeCategoryOrder(
                stateEvent
            )
            is ViewCategoriesStateEvent.GetAllOfCategories -> {
                val result = categoryRepository.getAllOfCategories(
                    stateEvent
                )
                DataState(
                    stateMessage = result.stateMessage,
                    data = ViewCategoriesViewState(categoryEntityList = result.data?.peekContent()),
                    stateEvent = result.stateEvent
                )
            }
        }

    override fun updateViewState(newViewStateView: ViewCategoriesViewState): ViewCategoriesViewState {
        val outDate = getCurrentViewStateOrNew()
        return ViewCategoriesViewState(
            newViewStateView.categoryEntityList ?: outDate.categoryEntityList,
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