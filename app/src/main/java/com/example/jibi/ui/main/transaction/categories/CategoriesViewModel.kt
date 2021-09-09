package com.example.jibi.ui.main.transaction.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.jibi.models.CategoryImages
import com.example.jibi.repository.cateogry.CategoryRepository
import com.example.jibi.ui.main.transaction.categories.state.CategoriesStateEvent
import com.example.jibi.ui.main.transaction.categories.state.CategoriesViewState
import com.example.jibi.ui.main.transaction.common.BaseViewModel
import com.example.jibi.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
//@MainScope
class CategoriesViewModel
@Inject
constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel<CategoriesViewState, CategoriesStateEvent>() {

    init {
        refreshCategoryList()
    }

    private val _categoriesImages: LiveData<List<CategoryImages>> =
        categoryRepository.getCategoryImages()
            .asLiveData()

    val categoriesImages: LiveData<List<CategoryImages>> = _categoriesImages


    override fun initNewViewState(): CategoriesViewState = CategoriesViewState()

    override suspend fun getResultByStateEvent(stateEvent: CategoriesStateEvent): DataState<CategoriesViewState> =
        when (stateEvent) {
            is CategoriesStateEvent.InsertCategory -> categoryRepository.insertCategory(
                stateEvent
            )
            is CategoriesStateEvent.DeleteCategory -> categoryRepository.deleteCategory(
                stateEvent
            )
            is CategoriesStateEvent.ChangeCategoryOrder -> categoryRepository.changeCategoryOrder(
                stateEvent
            )
            is CategoriesStateEvent.GetAllOfCategories -> categoryRepository.getAllOfCategories(
                stateEvent
            )
        }

    override fun updateViewState(newViewState: CategoriesViewState): CategoriesViewState {
        val outDate = getCurrentViewStateOrNew()
        return CategoriesViewState(
            newViewState.categoryList ?: outDate.categoryList,
            newViewState.insertedCategoryRow ?: outDate.insertedCategoryRow
        )
    }

    fun refreshCategoryList() {
        launchNewJob(
            CategoriesStateEvent.GetAllOfCategories
        )
    }

    fun newReorder(newOrder: HashMap<Int, Int>, type: Int) {
        launchNewJob(
            CategoriesStateEvent.ChangeCategoryOrder(
                newOrder,
                type
            )
        )
    }


}