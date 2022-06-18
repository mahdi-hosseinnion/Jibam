package com.ssmmhh.jibam.presentation.addedittransaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.data.source.repository.cateogry.CategoryRepository
import com.ssmmhh.jibam.data.source.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.presentation.addedittransaction.state.AddEditTransactionStateEvent
import com.ssmmhh.jibam.presentation.addedittransaction.state.AddEditTransactionViewState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import java.util.*
import javax.inject.Inject

class AddEditTransactionViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val currentLocale: Locale
) : BaseViewModel<AddEditTransactionViewState, AddEditTransactionStateEvent>() {

    private val _transactionCategory = MutableLiveData<Category>(null)
    val transactionCategory: LiveData<Category> = _transactionCategory

    private val _transactionDate = MutableLiveData<GregorianCalendar?>(null)
    val transactionDate: LiveData<GregorianCalendar?> = _transactionDate

    // Two-way databinding, exposing MutableLiveData
    val transactionMemo = MutableLiveData<String>()

    val categories: LiveData<List<Category>> =
        categoryRepository.observeAllOfCategories().asLiveData()

    private val _showSelectCategoryBottomSheet = MutableLiveData(false)
    val showSelectCategoryBottomSheet: LiveData<Boolean> =
        _showSelectCategoryBottomSheet.distinctUntilChanged()

    private var isNewTransaction: Boolean = true

    override suspend fun getResultByStateEvent(stateEvent: AddEditTransactionStateEvent): DataState<AddEditTransactionViewState> {
        return when (stateEvent) {
            else -> DataState.data()
        }
    }

    override fun updateViewState(newViewState: AddEditTransactionViewState): AddEditTransactionViewState =
        AddEditTransactionViewState()

    override fun initNewViewState(): AddEditTransactionViewState = AddEditTransactionViewState()

    fun startWithTransaction(transactionId: Int) {
        isNewTransaction = false
        TODO("Not yet implemented")
    }

    fun startNewTransaction() {
        isNewTransaction = true
        showSelectCategoryBottomSheet()
        _transactionDate.value = GregorianCalendar()

    }

    fun showSelectCategoryBottomSheet() {
        _showSelectCategoryBottomSheet.value = true
    }

    fun hideSelectCategoryBottomSheet() {
        _showSelectCategoryBottomSheet.value = false
    }

    fun setTransactionCategory(category: Category) {
        _transactionCategory.value = category
    }
}