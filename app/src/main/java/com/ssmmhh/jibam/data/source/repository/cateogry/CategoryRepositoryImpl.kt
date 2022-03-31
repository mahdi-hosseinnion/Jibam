package com.ssmmhh.jibam.data.source.repository.cateogry

import android.content.res.Resources
import androidx.annotation.StringRes
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.data.source.local.dao.CategoriesDao
import com.ssmmhh.jibam.data.source.local.dto.CategoryDto
import com.ssmmhh.jibam.data.source.local.entity.CategoryImageEntity
import com.ssmmhh.jibam.data.source.repository.safeCacheCall
import com.ssmmhh.jibam.data.util.CacheResponseHandler
import com.ssmmhh.jibam.data.util.CacheResult
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.feature_categories.addcategoires.state.AddCategoryStateEvent
import com.ssmmhh.jibam.feature_categories.addcategoires.state.AddCategoryViewState
import com.ssmmhh.jibam.feature_categories.viewcategories.state.ViewCategoriesStateEvent
import com.ssmmhh.jibam.feature_categories.viewcategories.state.ViewCategoriesViewState
import com.ssmmhh.jibam.util.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepositoryImpl
@Inject
constructor(
    private val categoriesDao: CategoriesDao,
    private val _resources: Resources
) : CategoryRepository {

    override fun getCategoryList(): Flow<List<CategoryDto>> =
        categoriesDao.observeCategories()

    override fun getCategoryImages(): Flow<List<CategoryImageEntity>> =
        categoriesDao.observeCategoriesImages()

    override suspend fun getAllOfCategories(
        stateEvent: StateEvent
    ): DataState<Event<List<Category>?>?> {

        val cacheResult = safeCacheCall {
            categoriesDao.getAllOfCategories()
        }
        return object : CacheResponseHandler<Event<List<Category>?>?, List<CategoryDto>>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: List<CategoryDto>): DataState<Event<List<Category>?>?> {
                return if (resultObj.isNotEmpty()) {
                    DataState.data(
                        Response(
                            message = "Successfully return all of categories",
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = Event(resultObj.map { it.toCategory() }),
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.error(
                        Response(
                            message = getString(R.string.getting_all_categories_error),
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Error
                        ),
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()

    }

    override suspend fun insertCategory(
        stateEvent: AddCategoryStateEvent.InsertCategory
    ): DataState<AddCategoryViewState> {

        //we don't care if we were able to increase all of categories order in same type
        //b/c use can reorder it manually later
        increaseAllOfOrdersByOne(stateEvent.categoryEntity.type)

        val cacheResult = safeCacheCall {
            categoriesDao.insertOrReplace(stateEvent.categoryEntity.copy(ordering = 0))
        }
        return object : CacheResponseHandler<AddCategoryViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<AddCategoryViewState> {
                return if (resultObj > 0) {
                    //success
                    DataState.data(
                        data = null,
                        response = Response(
                            message = getString(R.string.category_successfully_inserted),
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Success
                        )
                    )
                } else {
                    //failure
                    DataState.error(
                        response = Response(
                            message = getString(R.string.category_error_inserted),
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Error
                        )
                    )
                }
            }
        }.getResult()
    }

    private suspend fun increaseAllOfOrdersByOne(type: Int): CacheResult<Unit?> = safeCacheCall {
        categoriesDao.increaseAllOfOrdersByOne(type)
    }


    override suspend fun deleteCategory(
        stateEvent: ViewCategoriesStateEvent.DeleteCategory
    ): DataState<ViewCategoriesViewState> {
        val cacheResult = safeCacheCall {
            categoriesDao.deleteCategory(stateEvent.categoryId)
        }
        return object : CacheResponseHandler<ViewCategoriesViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<ViewCategoriesViewState> {
                return if (resultObj > 0) {
                    //success
                    DataState.data(
                        response = Response(
                            message = getString(R.string.category_successfully_deleted),
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Success
                        )
                    )
                } else {
                    //failure
                    DataState.error(
                        response = Response(
                            message = getString(R.string.category_error_deleted),
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Error
                        )
                    )
                }
            }
        }.getResult()
    }


    override suspend fun changeCategoryOrder(
        stateEvent: ViewCategoriesStateEvent.ChangeCategoryOrder
    ): DataState<ViewCategoriesViewState> {
        val allCategoriesResponse = safeCacheCall {
            categoriesDao.getAllOfCategoriesWithType(stateEvent.type)
        }
        if (allCategoriesResponse !is CacheResult.Success) {
            return changeCategoryOrderFailStateMessage(
                "Unable to get categories to update them",
                stateEvent
            )
        }

        val allCategories = allCategoriesResponse.value
            ?: return changeCategoryOrderFailStateMessage(
                "There is no category in database",
                stateEvent
            )

        val newOrder = stateEvent.newOrder

        var didAllCategoriesUpdatedSuccessfully = true

        for (item in allCategories) {
            newOrder[item.id]?.let { itemNewOrder ->
                if (item.ordering != itemNewOrder) {
                    val result = changeOrder(item.id, itemNewOrder)
                    if (result !is CacheResult.Success) {
                        didAllCategoriesUpdatedSuccessfully = false
                    }
                }

            }
        }
        return if (didAllCategoriesUpdatedSuccessfully) {
            DataState.data(
                response = Response(
                    message = "Successfully update ordering",
                    uiComponentType = UIComponentType.None,
                    messageType = MessageType.Success
                ),
                data = null,
                stateEvent = stateEvent
            )
        } else {
            changeCategoryOrderFailStateMessage(
                "At least on of the categories order did not updated",
                stateEvent
            )
        }
    }

    private fun changeCategoryOrderFailStateMessage(
        message: String,
        stateEvent: ViewCategoriesStateEvent.ChangeCategoryOrder
    ): DataState<ViewCategoriesViewState> = DataState.error(
        response = Response(
            message = message,
            uiComponentType = UIComponentType.Dialog,
            messageType = MessageType.Error
        ),
        stateEvent = stateEvent
    )


    private suspend fun changeOrder(categoryId: Int, newOrder: Int): CacheResult<Int?> =
        safeCacheCall {
            categoriesDao.updateOrder(categoryId, newOrder)
        }


    fun getString(@StringRes id: Int) = _resources.getString(id)

    companion object {
        const val CHANGE_CATEGORY_ORDER_SUCCESS = "Category order successfully changed"
    }
}