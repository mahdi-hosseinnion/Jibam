package com.example.jibi.repository.cateogry

import android.content.res.Resources
import android.util.Log
import androidx.annotation.StringRes
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.persistence.CategoriesDao
import com.example.jibi.repository.safeCacheCall
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionViewState
import com.example.jibi.ui.main.transaction.categories.state.CategoriesStateEvent
import com.example.jibi.ui.main.transaction.categories.state.CategoriesViewState
import com.example.jibi.util.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepositoryImpl
@Inject
constructor(
    private val categoriesDao: CategoriesDao,
    private val _resources: Resources
) : CategoryRepository {

    override fun getCategoryList(): Flow<List<Category>> =
        categoriesDao.getCategories()

    override fun getCategoryImages(): Flow<List<CategoryImages>> =
        categoriesDao.getCategoriesImages()

    override suspend fun getAllOfCategories(
        stateEvent: AddEditTransactionStateEvent.GetAllOfCategories
    ): DataState<AddEditTransactionViewState> {

        val cacheResult = safeCacheCall {
            categoriesDao.getAllOfCategories()
        }
        return object :
            CacheResponseHandler<AddEditTransactionViewState, List<Category>>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
            override suspend fun handleSuccess(resultObj: List<Category>): DataState<AddEditTransactionViewState> {
                return if (resultObj.isNotEmpty()) {
                    DataState.data(
                        Response(
                            message = "Successfully return all of categories",
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = AddEditTransactionViewState(
                            categoriesList = resultObj
                        ),
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
        stateEvent: CategoriesStateEvent.InsertCategory
    ): DataState<CategoriesViewState> {
        val cacheResult = safeCacheCall {
            categoriesDao.insertOrReplace(stateEvent.category)
        }
        return object : CacheResponseHandler<CategoriesViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<CategoriesViewState> {
                return if (resultObj > 0) {
                    //success
                    DataState.data(
                        data = CategoriesViewState(insertedCategoryRow = resultObj),
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

    override suspend fun deleteCategory(
        stateEvent: CategoriesStateEvent.DeleteCategory
    ): DataState<CategoriesViewState> {
        val cacheResult = safeCacheCall {
            categoriesDao.deleteCategory(stateEvent.categoryId)
        }
        return object : CacheResponseHandler<CategoriesViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<CategoriesViewState> {
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
        stateEvent: CategoriesStateEvent.ChangeCategoryOrder
    ): DataState<CategoriesViewState> {
        val changeSelectedCategoryOrderResponse = changeOrder(
            stateEvent.changeOrderFields.categoryId,
            stateEvent.changeOrderFields.newPosition
        )
        if (changeSelectedCategoryOrderResponse !is CacheResult.Success) {
            return changeCategoryOrderFail(
                "Unable to change order of selected category",
                stateEvent
            )
        }
        val betweenCategoriesResponse = getAllCategoriesBetweenSpecificOrder(
            categoryType = stateEvent.changeOrderFields.categoryType,
            from = stateEvent.changeOrderFields.lastPosition,
            to = stateEvent.changeOrderFields.newPosition
        )
        if (betweenCategoriesResponse !is CacheResult.Success || betweenCategoriesResponse.value == null) {
            return changeCategoryOrderFail("Unable to get categories to change order", stateEvent)
        }
        val betweenCategories = betweenCategoriesResponse.value
        Log.d(TAG, "changeCategoryOrder: betweenCategories: ${betweenCategories.map { it.name }}")
        for (item in betweenCategories) {
            val response = changeOrder(
                stateEvent.changeOrderFields.categoryId,
                stateEvent.changeOrderFields.newPosition
            )
            if (response !is CacheResult.Success) {
                return changeCategoryOrderFail(
                    "Unable to change order of ${item.name}",
                    stateEvent
                )
            }
        }
        return DataState.data(
            response = Response(
                message = CHANGE_CATEGORY_ORDER_SUCCESS,
                uiComponentType = UIComponentType.Toast,
                messageType = MessageType.Error
            ),
            data = null,
            stateEvent = stateEvent
        )
    }

    private fun changeCategoryOrderFail(
        message: String,
        stateEvent: CategoriesStateEvent.ChangeCategoryOrder
    ): DataState<CategoriesViewState> = DataState.error(
        response = Response(
            message = message,
            uiComponentType = UIComponentType.Dialog,
            messageType = MessageType.Error
        ),
        stateEvent = stateEvent
    )

    private fun changeCategoryOrderFail(
        message: String,
        stateEvent: CategoriesStateEvent.ChangeCategoryOrderNew
    ): DataState<CategoriesViewState> = DataState.error(
        response = Response(
            message = message,
            uiComponentType = UIComponentType.Dialog,
            messageType = MessageType.Error
        ),
        stateEvent = stateEvent
    )

    private suspend fun getAllCategoriesBetweenSpecificOrder(
        categoryType: Int,
        from: Int,
        to: Int
    ): CacheResult<List<Category>?> {
        var fromOrder: Int
        var toOrder: Int
        // from should no be greater then to
        if (to > from) {
            fromOrder = from.plus(1)
            toOrder = to.minus(1)
        } else if (to < from) {
            fromOrder = to.minus(1)
            toOrder = from.plus(1)
        } else {
            return CacheResult.GenericError("from and to should no be equal")
        }
        return safeCacheCall {
            categoriesDao.getAllCategoriesBetweenSpecificOrder(
                categoryType,
                fromOrder,
                toOrder
            )
        }
    }

    private suspend fun changeOrder(categoryId: Int, newOrder: Int): CacheResult<Int?> =
        safeCacheCall {
            categoriesDao.updateOrder(categoryId, newOrder)
        }


    fun getString(@StringRes id: Int) = _resources.getString(id)

    companion object {
        const val CHANGE_CATEGORY_ORDER_SUCCESS = "Category order successfully changed"
    }

    override suspend fun changeCategoryOrderNew(
        stateEvent: CategoriesStateEvent.ChangeCategoryOrderNew
    ): DataState<CategoriesViewState> {
        val allCategoriesResponse = safeCacheCall {
            categoriesDao.getAllOfCategoriesWithType(stateEvent.type)
        }
        if (allCategoriesResponse !is CacheResult.Success) {
            return changeCategoryOrderFail(
                "Unable to get categories to update them",
                stateEvent
            )
        }

        val allCategories = allCategoriesResponse.value
            ?: return changeCategoryOrderFail(
                "There is no category in database",
                stateEvent
            )

        val dataBaseOrder = allCategories.map { "id: ${it.id} order: ${it.ordering} |" }
        Log.d(TAG, "changeCategoryOrderNew: dataBaseOrder: $dataBaseOrder")
        val newOrder = stateEvent.newOrder
        Log.d(
            TAG,
            "changeCategoryOrderNew: newOrder     : ${newOrder.map { "id: ${it.key} order: ${it.value} |" }}"
        )
        var didAllCategoriesUpdatedSuccessfully = true

        for (item in allCategories) {
            newOrder[item.id]?.let {itemNewOrder->
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
                    uiComponentType = UIComponentType.Toast,
                    messageType = MessageType.Success
                ),
                data = null,
                stateEvent = stateEvent
            )
        } else {
            changeCategoryOrderFail(
                "At least on of the categories order did not updated",
                stateEvent
            )
        }
    }
}