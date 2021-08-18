package com.example.jibi.repository.cateogry

import android.content.res.Resources
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

    fun getString(@StringRes id: Int) = _resources.getString(id)

}