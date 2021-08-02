package com.example.jibi.ui.main.transaction.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.models.Transaction
import com.example.jibi.repository.cateogry.CategoryRepository
import com.example.jibi.ui.main.transaction.categories.state.CategoriesStateEvent
import com.example.jibi.ui.main.transaction.categories.state.CategoriesViewState
import com.example.jibi.ui.main.transaction.common.NewBaseViewModel
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsViewState
import com.example.jibi.util.DataState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class CategoriesViewModel
constructor(
    private val categoryRepository: CategoryRepository
) : NewBaseViewModel<CategoriesViewState, CategoriesStateEvent>() {

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