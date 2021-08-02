package com.example.jibi.ui.main.transaction.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.repository.cateogry.CategoryRepository
import com.example.jibi.ui.main.transaction.categories.state.CategoriesStateEvent
import com.example.jibi.ui.main.transaction.categories.state.CategoriesViewState
import com.example.jibi.ui.main.transaction.common.BaseViewModel
import com.example.jibi.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class CategoriesViewModel
@Inject
constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel<CategoriesViewState, CategoriesStateEvent>() {

    private val _categories: LiveData<List<Category>> = categoryRepository.getCategoryList()
        .asLiveData()

    val categories: LiveData<List<Category>> = _categories

    private val _categoriesImages: LiveData<List<CategoryImages>> = categoryRepository.getCategoryImages()
        .asLiveData()

    val categoriesImages: LiveData<List<CategoryImages>> = _categoriesImages


    override fun initNewViewState(): CategoriesViewState = CategoriesViewState()

    override suspend fun getResultByStateEvent(stateEvent: CategoriesStateEvent): DataState<CategoriesViewState> {
        TODO("Not yet implemented")
    }

    override fun updateViewState(newViewState: CategoriesViewState): CategoriesViewState {
        val outDate = getCurrentViewStateOrNew()
        return CategoriesViewState(
        )
    }
}