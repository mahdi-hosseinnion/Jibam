package com.ssmmhh.jibam.presentation.addedittransaction

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.data.model.Image
import com.ssmmhh.jibam.data.model.Transaction
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
import com.ssmmhh.jibam.util.DateUtils.toMilliSeconds
import com.ssmmhh.jibam.util.DateUtils.toSeconds
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
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

    private val _isSubmitFabEnabled = MutableLiveData(true)
    val isSubmitFabEnabled: LiveData<Boolean> = _isSubmitFabEnabled

    private val _navigateBackEvent = MutableLiveData<Event<Unit>>()
    val navigateBackEvent: LiveData<Event<Unit>> = _navigateBackEvent

    var isNewTransaction: Boolean = true
        private set

    /**
     * Prevent start functions from starting after configuration change.
     */
    private var isThisTheFirstLaunch: Boolean = true

    override suspend fun getResultByStateEvent(stateEvent: AddEditTransactionStateEvent): DataState<AddEditTransactionViewState> {
        return when (stateEvent) {
            is AddEditTransactionStateEvent.InsertTransaction -> {
                val result = transactionRepository.insertTransaction(stateEvent)
                withContext(Main) {
                    if (result.stateMessage?.response?.messageType == MessageType.Success) {
                        _navigateBackEvent.value = Event(Unit)
                    }
                    _isSubmitFabEnabled.value = true
                }
                DataState(
                    stateMessage = result.stateMessage,
                    data = AddEditTransactionViewState(),
                    stateEvent = result.stateEvent
                )
            }
            is AddEditTransactionStateEvent.GetTransactionById -> {
                val result = transactionRepository.getTransactionById(stateEvent)
                withContext(Main) {
                    loadTransaction(result.data)
                }
                DataState(
                    stateMessage = result.stateMessage,
                    data = AddEditTransactionViewState(),
                    stateEvent = result.stateEvent
                )
            }
            is AddEditTransactionStateEvent.GetCategoryById -> {
                //TODO no implemnted yet("GetCategoryById")
                //TODO("Not emplemnted yet")
                throw Exception("GetCategoryById")
            }
        }
    }

    private fun loadTransaction(transaction: Transaction?) {
        if (transaction == null) {
            Log.e(TAG, "loadTransaction: Transaction is Null.")
            showErrorSnackBar(R.string.unable_to_get_the_transaciton)
            return
        }
        calculatorText.value = transaction.money.toPlainString()
        transactionMemo.value = transaction.memo
        _transactionDate.value = GregorianCalendar().apply {
            timeInMillis = transaction.date.toMilliSeconds()
        }
        launchNewJob(
            AddEditTransactionStateEvent.GetCategoryById(transaction.categoryId)
        )
    }

    override fun updateViewState(newViewState: AddEditTransactionViewState): AddEditTransactionViewState =
        AddEditTransactionViewState()

    override fun initNewViewState(): AddEditTransactionViewState = AddEditTransactionViewState()

    fun startWithTransaction(transactionId: Int) {
        if (!isThisTheFirstLaunch) return
        isThisTheFirstLaunch = false

        isNewTransaction = false
        launchNewJob(
            stateEvent = AddEditTransactionStateEvent.GetTransactionById(transactionId)
        )
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
        val date = _transactionDate.value ?: if (isNewTransaction)
            GregorianCalendar()
        else
            throw RuntimeException("setTransactionDate() was called but _transactionDate is null.")

        date.set(GregorianCalendar.HOUR_OF_DAY, hourOfDay)
        date.set(GregorianCalendar.MINUTE, minute)
        _transactionDate.value = date
    }

    fun setTransactionDate(year: Int, month: Int, dayOfMonth: Int) {
        val date = _transactionDate.value ?: if (isNewTransaction)
            GregorianCalendar()
        else
            throw RuntimeException("setTransactionDate() was called but _transactionDate is null.")

        date.set(year, month, dayOfMonth)
        _transactionDate.value = date
    }

    fun showDiscardOrSaveDialog() {
        val callback = object : DiscardOrSaveCallback {
            override fun save() {
                if (isNewTransaction) {
                    insertTransaction()
                } else {
                    updateTransaction()
                }
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

    fun insertTransaction() {
        _isSubmitFabEnabled.value = false
        getTransactionIfItsReadyToInsert()?.let {
            launchNewJob(AddEditTransactionStateEvent.InsertTransaction(it))
        } ?: kotlin.run {
            _isSubmitFabEnabled.value = true
        }
    }

    /**
     * Returns a transaction if all of necessary fields are filled, otherwise null.
     */
    private fun getTransactionIfItsReadyToInsert(): Transaction? {

        val calculatedResult = calculatorResult.value.toString().removeSeparateSign()
        if (calculatedResult.isEmpty()) {
            showErrorSnackBar(R.string.pls_insert_some_money)
            return null
        }

        val money = calculatedResult.toBigDecimalOrNull()
        if (money == null || money < ZERO) {
            showErrorSnackBar(R.string.pls_insert_valid_amount_of_money)
            return null
        }

        val category = transactionCategory.value
        if (category == null) {
            showErrorSnackBar(R.string.pls_select_category)
            return null
        }
        val date = transactionDate.value?.timeInMillis?.toSeconds()
        if (date == null) {
            showErrorSnackBar(R.string.enter_date_and_time_for_transaction)
            return null
        }
        return Transaction(
            id = 0,
            money = if (category.isIncomeCategory) money else money.negate(),
            memo = transactionMemo.value,
            categoryId = category.id,
            categoryName = category.name,
            categoryImage = category.image,
            date = date,
        )

    }

    private fun showErrorSnackBar(@StringRes message: Int) {
        //TODO ("show snack bar")
        addToMessageStack(
            message = intArrayOf(message),
            uiComponentType = UIComponentType.Toast,
            messageType = MessageType.Error
        )
    }

    fun updateTransaction() {

    }
}