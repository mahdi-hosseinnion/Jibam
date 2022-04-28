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
import com.ssmmhh.jibam.data.util.MessageType
import com.ssmmhh.jibam.data.util.UIComponentType
import com.ssmmhh.jibam.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddCategoryViewModel
@Inject
constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel<AddCategoryViewState, AddCategoryStateEvent>() {

    val images: LiveData<List<CategoryImageEntity>> =
        categoryRepository.getCategoryImages().asLiveData()

    // Two-way databinding, exposing MutableLiveData
    val categoryName = MutableLiveData<String>()

    private val _isAddCategoryButtonEnabled = MutableLiveData(true)
    val isAddCategoryButtonEnabled: LiveData<Boolean> = _isAddCategoryButtonEnabled

    private val _categorySuccessfullyInsertedEvent = MutableLiveData<Event<Unit>>()

    val categorySuccessfullyInsertedEvent: LiveData<Event<Unit>> =
        _categorySuccessfullyInsertedEvent

    override fun initNewViewState(): AddCategoryViewState = AddCategoryViewState()

    override suspend fun getResultByStateEvent(
        stateEvent: AddCategoryStateEvent
    ): DataState<AddCategoryViewState> = when (stateEvent) {

        is AddCategoryStateEvent.InsertCategory -> {
            val result = categoryRepository.insertCategory(stateEvent)
            withContext(Dispatchers.Main) {
                if (result.stateMessage?.response?.messageType == MessageType.Success) {
                    _categorySuccessfullyInsertedEvent.value = Event(Unit)
                } else {
                    _isAddCategoryButtonEnabled.value = true
                }
            }
            result
        }
    }

    override fun updateViewState(newViewState: AddCategoryViewState): AddCategoryViewState {
        val outDated = getCurrentViewStateOrNew()
        return AddCategoryViewState(
            categoryImage = newViewState.categoryImage ?: outDated.categoryImage,
            categoryType = newViewState.categoryType ?: outDated.categoryType
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

    fun insertCategory() {
        val category: CategoryEntity = getCategoryIfItIsReadyToInsert() ?: return
        _isAddCategoryButtonEnabled.value = false
        launchNewJob(
            AddCategoryStateEvent.InsertCategory(
                categoryEntity = category
            )
        )
    }

    private fun getCategoryIfItIsReadyToInsert(): CategoryEntity? {

        val name = categoryName.value
        if (name.isNullOrBlank()) {
            addErrorToastToStackMessage(message = intArrayOf(R.string.category_should_have_name))
            return null
        }

        val type = getCurrentViewStateOrNew().categoryType
        if (type == null) {
            addErrorToastToStackMessage(message = intArrayOf(R.string.unable_to_recognize_category_type))
            return null
        }

        val imageId = getCurrentViewStateOrNew().categoryImage?.id
        if (imageId == null) {
            addErrorToastToStackMessage(message = intArrayOf(R.string.pls_select_image_for_category))
            return null
        }

        return CategoryEntity(id = 0, ordering = 0, name = name, type = type, imageId = imageId)

    }

    private fun addErrorToastToStackMessage(message: IntArray) {
        //TODO ("Show snack bar instead of toast")
        addToMessageStack(
            message = message,
            uiComponentType = UIComponentType.Toast,
            messageType = MessageType.Error
        )
    }

}