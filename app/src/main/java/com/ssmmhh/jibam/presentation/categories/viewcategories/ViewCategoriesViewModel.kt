package com.ssmmhh.jibam.presentation.categories.viewcategories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssmmhh.jibam.data.source.repository.cateogry.CategoryRepository
import com.ssmmhh.jibam.presentation.categories.viewcategories.state.ViewCategoriesStateEvent
import com.ssmmhh.jibam.presentation.categories.viewcategories.state.ViewCategoriesViewState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class ViewCategoriesViewModel
@Inject
constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel<ViewCategoriesViewState, ViewCategoriesStateEvent>() {

    init {
        refreshCategoryList()
    }

    private val _openAddCategoryEvent = MutableLiveData<Event<Unit>>()
    val openAddCategoryEvent: LiveData<Event<Unit>> = _openAddCategoryEvent

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    fun openAddCategory() {
        _openAddCategoryEvent.value = Event(Unit)
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