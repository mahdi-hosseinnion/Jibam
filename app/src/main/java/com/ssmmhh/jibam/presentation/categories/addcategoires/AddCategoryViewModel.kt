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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddCategoryViewModel
@Inject
constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel<AddCategoryViewState, AddCategoryStateEvent>() {

    val images: LiveData<List<AddCategoryRecyclerViewItem>> =
        categoryRepository.getCategoryImages().map { items ->
            return@map groupImagesByGroupName(items)
        }.asLiveData()

    // Two-way databinding, exposing MutableLive Data
    val categoryName = MutableLiveData<String>()

    private val _categoryType: MutableLiveData<Int> = MutableLiveData()
    val categoryType: LiveData<Int> = _categoryType

    private val _categoryImage: MutableLiveData<CategoryImageEntity> = MutableLiveData()
    val categoryImage: LiveData<CategoryImageEntity> = _categoryImage

    var selectedImagePositionInRecyclerView: Int? = null
        private set

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
        return AddCategoryViewState()
    }

    private fun groupImagesByGroupName(list: List<CategoryImageEntity>): List<AddCategoryRecyclerViewItem> {
        val listGroupedByGroupName: Map<String, List<CategoryImageEntity>> =
            list.sortedBy { it.groupName }.groupBy { it.groupName }
        val result = ArrayList<AddCategoryRecyclerViewItem>()
        for ((groupName, images) in listGroupedByGroupName) {
            result.add(AddCategoryRecyclerViewItem.Header(groupName))
            result.addAll(
                images.map { AddCategoryRecyclerViewItem.CategoryImage(it) }
            )
        }
        return result
    }

    fun setCategoryType(type: Int) {
        _categoryType.value = type
    }

    fun setCategoryImage(categoryImageEntity: CategoryImageEntity, position: Int?) {
        selectedImagePositionInRecyclerView = position
        _categoryImage.value = categoryImageEntity
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

        val type = categoryType.value
        if (type == null) {
            addErrorToastToStackMessage(message = intArrayOf(R.string.unable_to_recognize_category_type))
            return null
        }

        val imageId = categoryImage.value?.id
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