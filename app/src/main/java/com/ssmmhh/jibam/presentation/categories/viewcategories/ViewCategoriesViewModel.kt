package com.ssmmhh.jibam.presentation.categories.viewcategories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.data.source.repository.cateogry.CategoryRepository
import com.ssmmhh.jibam.presentation.categories.viewcategories.state.ViewCategoriesStateEvent
import com.ssmmhh.jibam.presentation.categories.viewcategories.state.ViewCategoriesViewState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class ViewCategoriesViewModel
@Inject
constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel<ViewCategoriesViewState, ViewCategoriesStateEvent>() {

    private val _allOfCategories: Flow<List<Category>> = categoryRepository.observeAllOfCategories()

    private val _expensesCategories: LiveData<List<Category>> = _allOfCategories.map {
        it.filter { category ->
            category.isExpensesCategory
        }
    }.asLiveData()

    val expensesCategories: LiveData<List<Category>> = _expensesCategories

    private val _incomeCategories: LiveData<List<Category>> = _allOfCategories.map {
        it.filter { category ->
            category.isIncomeCategory
        }
    }.asLiveData()

    val incomeCategories: LiveData<List<Category>> = _incomeCategories

    private val _openAddCategoryEvent = MutableLiveData<Event<Unit>>()
    val openAddCategoryEvent: LiveData<Event<Unit>> = _openAddCategoryEvent

    override fun initNewViewState(): ViewCategoriesViewState = ViewCategoriesViewState()

    override suspend fun getResultByStateEvent(stateEvent: ViewCategoriesStateEvent): DataState<ViewCategoriesViewState> =
        when (stateEvent) {
            is ViewCategoriesStateEvent.DeleteCategory -> categoryRepository.deleteCategory(
                stateEvent
            )
            is ViewCategoriesStateEvent.ChangeCategoryOrder -> categoryRepository.changeCategoryOrder(
                stateEvent
            )
        }

    override fun updateViewState(newViewStateView: ViewCategoriesViewState): ViewCategoriesViewState {
        return ViewCategoriesViewState()
    }

    fun openAddCategory() {
        _openAddCategoryEvent.value = Event(Unit)
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