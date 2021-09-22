package com.example.jibi.repository.cateogry

import android.content.res.Resources
import androidx.annotation.StringRes
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.persistence.CategoriesDao
import com.example.jibi.repository.safeCacheCall
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionViewState
import com.example.jibi.ui.main.transaction.categories.viewcategories.state.ViewCategoriesStateEvent
import com.example.jibi.ui.main.transaction.categories.viewcategories.state.ViewCategoriesViewState
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
        stateEvent: DetailEditTransactionStateEvent.GetAllOfCategories
    ): DataState<DetailEditTransactionViewState> {

        val cacheResult = safeCacheCall {
            categoriesDao.getAllOfCategories()
        }
        return object :
            CacheResponseHandler<DetailEditTransactionViewState, List<Category>>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
            override suspend fun handleSuccess(resultObj: List<Category>): DataState<DetailEditTransactionViewState> {
                return if (resultObj.isNotEmpty()) {
                    DataState.data(
                        Response(
                            message = "Successfully return all of categories",
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = DetailEditTransactionViewState(
                            allOfCategories = resultObj
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

    override suspend fun getAllOfCategories(
        stateEvent: ViewCategoriesStateEvent.GetAllOfCategories
    ): DataState<ViewCategoriesViewState> {

        val cacheResult = safeCacheCall {
            categoriesDao.getAllOfCategories()
        }
        return object :
            CacheResponseHandler<ViewCategoriesViewState, List<Category>>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
            override suspend fun handleSuccess(resultObj: List<Category>): DataState<ViewCategoriesViewState> {
                return if (resultObj.isNotEmpty()) {
                    DataState.data(
                        Response(
                            message = "Successfully return all of categories",
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = ViewCategoriesViewState(
                            categoryList = resultObj
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

    override suspend fun getAllOfCategories(
        stateEvent: InsertTransactionStateEvent.GetAllOfCategories
    ):
            DataState<InsertTransactionViewState> {

        val cacheResult = safeCacheCall {
            categoriesDao.getAllOfCategories()
        }
        return object :
            CacheResponseHandler<InsertTransactionViewState, List<Category>>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
            override suspend fun handleSuccess(resultObj: List<Category>): DataState<InsertTransactionViewState> {
                return if (resultObj.isNotEmpty()) {
                    DataState.data(
                        Response(
                            message = "Successfully return all of categories",
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = InsertTransactionViewState(
                            allOfCategories = resultObj
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
        stateEvent: ViewCategoriesStateEvent.InsertCategory
    ): DataState<ViewCategoriesViewState> {
        val cacheResult = safeCacheCall {
            categoriesDao.insertOrReplace(stateEvent.category)
        }
        return object : CacheResponseHandler<ViewCategoriesViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<ViewCategoriesViewState> {
                return if (resultObj > 0) {
                    //success
                    DataState.data(
                        data = ViewCategoriesViewState(insertedCategoryRow = resultObj),
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

        val dataBaseOrder = allCategories.map { "id: ${it.id} order: ${it.ordering} |" }

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