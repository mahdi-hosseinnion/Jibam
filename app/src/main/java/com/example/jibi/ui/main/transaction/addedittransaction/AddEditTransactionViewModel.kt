package com.example.jibi.ui.main.transaction.addedittransaction
/*
import android.util.Log
import com.example.jibi.models.Category
import com.example.jibi.models.Transaction
import com.example.jibi.models.TransactionEntity
import com.example.jibi.repository.cateogry.CategoryRepository
import com.example.jibi.repository.tranasction.TransactionRepository
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionViewState
import com.example.jibi.ui.main.transaction.addedittransaction.state.PresenterState
import com.example.jibi.ui.main.transaction.common.BaseViewModel
import com.example.jibi.util.Constants.EXPENSES_OTHER_CATEGORY_ID
import com.example.jibi.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
//@MainScope
class AddEditTransactionViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : BaseViewModel<AddEditTransactionViewState, AddEditTransactionStateEvent>() {
    private val TAG = "AddEditTransactionViewM"

    init {
        launchNewJob(AddEditTransactionStateEvent.GetAllOfCategories)
    }

    override fun initNewViewState(): AddEditTransactionViewState = AddEditTransactionViewState()

    override suspend fun getResultByStateEvent(stateEvent: AddEditTransactionStateEvent): DataState<AddEditTransactionViewState> =
        when (stateEvent) {
            is AddEditTransactionStateEvent.InsertTransaction -> transactionRepository.insertTransaction(
                stateEvent
            )
            is AddEditTransactionStateEvent.DeleteTransaction -> transactionRepository.deleteTransaction(
                stateEvent
            )
            is AddEditTransactionStateEvent.GetTransactionById -> transactionRepository.getTransactionById(
                stateEvent
            )
            is AddEditTransactionStateEvent.GetAllOfCategories -> categoryRepository.getAllOfCategories(
                stateEvent
            )
        }

    override fun updateViewState(newViewState: AddEditTransactionViewState): AddEditTransactionViewState {
        val outDate = getCurrentViewStateOrNew()
        return AddEditTransactionViewState(
            transaction = newViewState.transaction ?: outDate.transaction,
            categoryType = newViewState.categoryType ?: outDate.categoryType,
            categoriesList = newViewState.categoriesList ?: outDate.categoriesList,
            insertedTransactionRawId = newViewState.insertedTransactionRawId
                ?: outDate.insertedTransactionRawId,
            successfullyDeletedTransactionIndicator = newViewState.successfullyDeletedTransactionIndicator
                ?: outDate.successfullyDeletedTransactionIndicator,
            presenterState = newViewState.presenterState
                ?: outDate.presenterState
        )
    }

    fun getCategoriesList(): List<Category> {
        val result = getCurrentViewStateOrNew().categoriesList
        if (result == null) {
            launchNewJob(AddEditTransactionStateEvent.GetAllOfCategories)
            return emptyList()
        }
        return result
    }


    fun getSelectedCategoryId(): Int? = getCurrentViewStateOrNew().transaction?.categoryId
    fun getTransactionId(): Int? = getCurrentViewStateOrNew().transaction?.id

    fun setPresenterState(newState: PresenterState) {
        setViewState(
            AddEditTransactionViewState(
                presenterState = newState
            )
        )
    }

    fun getTransactionCategoryType(): Int? = viewState.value?.categoryType
//        val result = viewState.value?.transactionCategory
//        if (result != null) {
//            return result
//        }
//        val defaultCategory = findCategoryById(EXPENSES_OTHER_CATEGORY_ID)
//        if (defaultCategory != null) {
//            setTransactionCategory(defaultCategory)
//            return defaultCategory
//        }
//        return viewState.value?.categoriesList?.get(0)
//    }

//    private fun findCategoryById(id: Int): Category? {
//        viewState.value?.categoriesList?.let {
//            for (item in it) {
//                if (item.id == id) {
//                    return item
//                }
//            }
//        }
//        return null
//    }

    fun setTransactionCategoryType(category: Category, date: Int) {
        val outdatedTransaction =
            viewState.value?.transaction ?: Transaction(
                id = 0,
                money = 0.0,
                memo = null,
                categoryId = category.id,
                categoryName = category.name,
                categoryImage = category.img_res,
                date = date
            )
        setViewState(
            AddEditTransactionViewState(
                transaction = outdatedTransaction.copy(
                    categoryId = category.id,
                    categoryName = category.name,
                    categoryImage = category.img_res,
                ),
                categoryType = category.type
            )
        )
    }

    fun insertTransaction(transaction: TransactionEntity) {
        launchNewJob(
            AddEditTransactionStateEvent.InsertTransaction(
                transaction
            )
        )
    }

    fun isThisNewTransaction(): Boolean {
        TODO("Not yet implemented")
    }


/*    fun checkForTransactionCategoryToNotBeNull() {
        if (viewState.value?.transactionCategory != null) {
            return
        }
        Log.d(TAG, "setTransactionCategoryToNewDefault: called")
        viewState.value?.categoriesList?.let {
            val default = findCategoryById(EXPENSES_OTHER_CATEGORY_ID)
            if (default != null) {
                setTransactionCategory(default)
                return
            }
            val first = it[0]
            setTransactionCategory(first)
            Log.d(TAG, "setTransactionCategoryToNewDefault: default: $default")
            Log.d(TAG, "setTransactionCategoryToNewDefault: first: $first")

        }
    }
}*/