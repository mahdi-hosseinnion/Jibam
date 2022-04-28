package com.ssmmhh.jibam.presentation.categories.addcategoires

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity
import com.ssmmhh.jibam.data.source.local.entity.CategoryImageEntity
import com.ssmmhh.jibam.data.source.repository.cateogry.CategoryRepository
import com.ssmmhh.jibam.presentation.categories.addcategoires.state.AddCategoryStateEvent
import com.ssmmhh.jibam.presentation.categories.addcategoires.state.AddCategoryViewState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.data.util.DataState
import java.util.*
import javax.inject.Inject
import kotlin.jvm.Throws

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

    @Throws(IllegalArgumentException::class)
    fun insertCategory(categoryName: String) {
        val category = CategoryEntity(
            id = 0,
            ordering = 0,
            name = categoryName,

            type = getCurrentViewStateOrNew().categoryType
                ?: throw IllegalArgumentException("AddCategoryViewModel: insertCategory: category type in view state  is null"),

            imageId = getCurrentViewStateOrNew().categoryImage?.id
                ?: throw IllegalArgumentException("AddCategoryViewModel: insertCategory: category image in view state  is null")
        )
        launchNewJob(
            AddCategoryStateEvent.InsertCategory(
                categoryEntity = category
            )
        )

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

}