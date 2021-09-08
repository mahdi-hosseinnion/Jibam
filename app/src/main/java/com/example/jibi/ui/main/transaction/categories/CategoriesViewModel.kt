package com.example.jibi.ui.main.transaction.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.repository.cateogry.CategoryRepository
import com.example.jibi.ui.main.transaction.categories.state.CategoriesStateEvent
import com.example.jibi.ui.main.transaction.categories.state.CategoriesViewState
import com.example.jibi.ui.main.transaction.categories.state.ChangeOrderFields
import com.example.jibi.ui.main.transaction.common.BaseViewModel
import com.example.jibi.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.HashMap
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
//@MainScope
class CategoriesViewModel
@Inject
constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel<CategoriesViewState, CategoriesStateEvent>() {

    private val _categories: LiveData<List<Category>> = categoryRepository.getCategoryList()
        .asLiveData()

    val categories: LiveData<List<Category>> = _categories

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
            is CategoriesStateEvent.ChangeCategoryOrderNew -> categoryRepository.changeCategoryOrderNew(
                stateEvent
            )
        }

    override fun updateViewState(newViewState: CategoriesViewState): CategoriesViewState {
        val outDate = getCurrentViewStateOrNew()
        return CategoriesViewState(
            newViewState.insertedCategoryRow ?: outDate.insertedCategoryRow
        )
    }

    /**
     * change order function should run in order
     * ex) if user change A order from 1 to 2 then 2 to 3 we should run it in order
     * first 1 to 2 then 2 to 3
     */
    private val _changeOrderStack: ArrayList<ChangeOrderFields> =
        ArrayList<ChangeOrderFields>()

    val changeOrderStack = _changeOrderStack

    fun addToChangeOrderStack(changeOrderFields: ChangeOrderFields) {
        _changeOrderStack.add(changeOrderFields)
    }

    fun removeFromChangeOrderStack(index: Int = 0) {
        _changeOrderStack.removeAt(index)
    }

    fun clearChangeOrderStack() {
        _changeOrderStack.clear()
    }

    private fun thereIsActiveChangeOrder(): Boolean {
        val activeJobs = getAllActiveJobs()
        for (jobId in activeJobs) {
            if (jobId.contains(CategoriesStateEvent.ChangeCategoryOrder.NAME)) {
                return true
            }
        }
        return false
    }

    fun insertPendingChangeOrder() {
        if (!changeOrderStack.isNullOrEmpty()) {
            changeCategoryOrder(
                changeOrderStack[0]
            )
            removeFromChangeOrderStack()
        }
    }

    private fun changeCategoryOrder(changeOrderFields: ChangeOrderFields) {
        if (!thereIsActiveChangeOrder()) {
            launchNewJob(
                CategoriesStateEvent.ChangeCategoryOrder(
                    changeOrderFields
                )
            )
        }

    }

    fun newReorder(newOrder: HashMap<Int, Int>,type:Int) {
        launchNewJob(
            CategoriesStateEvent.ChangeCategoryOrderNew(
                newOrder,
                type
            )
        )
    }

}