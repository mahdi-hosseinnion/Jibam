package com.ssmmhh.jibam.presentation.addedittransaction

import androidx.lifecycle.*
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.data.source.repository.cateogry.CategoryRepository
import com.ssmmhh.jibam.data.source.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.data.util.DataState
import com.ssmmhh.jibam.data.util.DiscardOrSaveCallback
import com.ssmmhh.jibam.data.util.MessageType
import com.ssmmhh.jibam.data.util.UIComponentType
import com.ssmmhh.jibam.presentation.addedittransaction.state.AddEditTransactionStateEvent
import com.ssmmhh.jibam.presentation.addedittransaction.state.AddEditTransactionViewState
import com.ssmmhh.jibam.presentation.common.BaseViewModel
import com.ssmmhh.jibam.util.*
import kotlinx.coroutines.flow.combine
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

    // Two-way databinding, exposing MutableLiveData
    val calculatorText = MutableLiveData<String>()

    val categories: LiveData<List<Category>> =
        categoryRepository.observeAllOfCategories().asLiveData()

    val calculatorResult: LiveData<String> = calculatorText.map {
        val text = it.toString().remove3By3Separators()
        if (text.isEmpty()) return@map text

        val calculatedResult = TextCalculator.calculateResult(text)
        val finalNumberText = localizeDoubleNumber(calculatedResult.toDoubleOrNull(), currentLocale)
            ?: return@map ""
        return@map finalNumberText
            .convertFarsiDigitsToEnglishDigits()
            .toBigDecimalOrNull()
            ?.let { separate3By3(it, currentLocale) }
            ?: ""
    }

    val isCalculatorResultVisible: LiveData<Boolean> =
        combine(
            calculatorText.asFlow(),
            calculatorResult.asFlow()
        ) { text, result ->
            return@combine text != result
        }.asLiveData()

    private val _showSelectCategoryBottomSheet = MutableLiveData(false)
    val showSelectCategoryBottomSheet: LiveData<Boolean> =
        _showSelectCategoryBottomSheet.distinctUntilChanged()

    private val _showTimePickerDialog = MutableLiveData(false)
    val showTimePickerDialog: LiveData<Boolean> = _showTimePickerDialog

    private val _showDatePickerDialog = MutableLiveData(false)
    val showDatePickerDialog: LiveData<Boolean> = _showDatePickerDialog

    private val _navigateBackEvent = MutableLiveData<Event<Unit>>()
    val navigateBackEvent: LiveData<Event<Unit>> = _navigateBackEvent

    private var isNewTransaction: Boolean = true

    /**
     * Prevent start functions from starting after configuration change.
     */
    private var isThisTheFirstLaunch: Boolean = true

    override suspend fun getResultByStateEvent(stateEvent: AddEditTransactionStateEvent): DataState<AddEditTransactionViewState> {
        return when (stateEvent) {
            else -> DataState.data()
        }
    }

    override fun updateViewState(newViewState: AddEditTransactionViewState): AddEditTransactionViewState =
        AddEditTransactionViewState()

    override fun initNewViewState(): AddEditTransactionViewState = AddEditTransactionViewState()

    fun startWithTransaction(transactionId: Int) {
        if (!isThisTheFirstLaunch) return
        isThisTheFirstLaunch = false

        isNewTransaction = false
        TODO("Not yet implemented")
    }

    fun startNewTransaction() {
        if (!isThisTheFirstLaunch) return
        isThisTheFirstLaunch = false

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

    fun showTimePickerDialog() {
        _showTimePickerDialog.value = true
    }

    fun hideTimePickerDialog() {
        _showTimePickerDialog.value = false
    }

    fun showDatePickerDialog() {
        _showDatePickerDialog.value = true
    }

    fun hideDatePickerDialog() {
        _showDatePickerDialog.value = false
    }

    fun setTransactionDateTime(hourOfDay: Int, minute: Int) {
        val date = _transactionDate.value ?: return
        date.set(GregorianCalendar.HOUR_OF_DAY, hourOfDay)
        date.set(GregorianCalendar.MINUTE, minute)
        _transactionDate.value = date
    }

    fun setTransactionDate(year: Int, month: Int, dayOfMonth: Int) {
        val date = _transactionDate.value ?: return
        date.set(year, month, dayOfMonth)
        _transactionDate.value = date
    }

    fun showDiscardOrSaveDialog() {
        val callback = object : DiscardOrSaveCallback {
            override fun save() {
                insertTransaction()
            }

            override fun discard() {
                _navigateBackEvent.value = Event(Unit)
            }

            override fun cancel() {}
        }
        addToMessageStack(
            message = intArrayOf(R.string.you_changes_have_not_saved),
            uiComponentType = UIComponentType.DiscardOrSaveDialog(callback),
            messageType = MessageType.Info
        )
    }

    private fun insertTransaction() {

    }
}