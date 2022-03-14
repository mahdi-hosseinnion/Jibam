package com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.persistence.entities.CategoryImageEntity
import com.ssmmhh.jibam.repository.cateogry.CategoryRepository
import com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires.state.AddCategoryStateEvent
import com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires.state.AddCategoryViewState
import com.ssmmhh.jibam.ui.main.transaction.common.BaseViewModel
import com.ssmmhh.jibam.util.DataState
import javax.inject.Inject

class AddCategoryViewModel
@Inject
constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel<AddCategoryViewState, AddCategoryStateEvent>() {

    override fun initNewViewState(): AddCategoryViewState = AddCategoryViewState()

    override suspend fun getResultByStateEvent(
        stateEvent: AddCategoryStateEvent
    ): DataState<AddCategoryViewState> = when (stateEvent) {
        is AddCategoryStateEvent.InsertCategory -> categoryRepository.insertCategory(stateEvent)
    }

    override fun updateViewState(newViewState: AddCategoryViewState): AddCategoryViewState {
        val outDated = getCurrentViewStateOrNew()
        return AddCategoryViewState(
            categoryImage = newViewState.categoryImage ?: outDated.categoryImage,
            categoryType = newViewState.categoryType ?: outDated.categoryType
        )
    }

    private val _categoriesImageEntity: LiveData<List<CategoryImageEntity>> =
        categoryRepository.getCategoryImages()
            .asLiveData()

    val categoriesImageEntity: LiveData<List<CategoryImageEntity>> = _categoriesImageEntity

    fun insertCategory(categoryName: String): Int {
        val category = CategoryEntity(
            id = 0,
            ordering = 0,
            name = categoryName,

            type = getCurrentViewStateOrNew().categoryType
                ?: return R.string.unable_to_recognize_category_type,

            img_res = getCurrentViewStateOrNew().categoryImage?.image_res
                ?: return R.string.pls_select_image_for_category
        )
        launchNewJob(
            AddCategoryStateEvent.InsertCategory(
                categoryEntity = category
            )
        )

        return INSERT_CATEGORY_SUCCESS_MARKER
    }

    fun setCategoryType(categoryType: Int) {
        setViewState(
            AddCategoryViewState(
                categoryType = categoryType
            )
        )
    }

    fun setCategoryImage(categoryImageEntity: CategoryImageEntity) {
        setViewState(
            AddCategoryViewState(
                categoryImage = categoryImageEntity
            )
        )
    }

    companion object {
        const val INSERT_CATEGORY_SUCCESS_MARKER = -1
    }
}