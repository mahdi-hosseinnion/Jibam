package com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state

import android.util.Log
import com.example.jibi.models.Category
import com.example.jibi.models.Transaction
import com.example.jibi.models.TransactionEntity
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionPresenterState
import com.example.jibi.util.Event
import com.example.jibi.util.StateEvent
import kotlinx.coroutines.flow.*
import java.util.*

data class DetailEditTransactionViewState(
    // default transaction used to determine if user update transaction or did not
    val defaultTransaction: Transaction? = null,
    // transaction used to maintain inserted data during configuration change
    val transaction: Transaction? = null,
    val moneyStr: String? = null,
    val transactionCategoryType: Int? = null,
    val combineCalender: GregorianCalendar? = null,
    val allOfCategories: List<Category>? = null,
    val successfullyDeletedTransactionIndicator: Int? = null,
    val presenterState: Event<DetailEditTransactionPresenterState>? = null

)

sealed class DetailEditTransactionPresenterState() {

    object SelectingCategoryState : DetailEditTransactionPresenterState()

    object EnteringAmountOfMoneyState : DetailEditTransactionPresenterState()

    object ChangingDateState : DetailEditTransactionPresenterState()

    object ChangingTimeState : DetailEditTransactionPresenterState()

    object AddingNoteState : DetailEditTransactionPresenterState()

    object NoneState : DetailEditTransactionPresenterState()

}

class SubmitButtonState() {

    private var defaultTransaction: TransactionEntity? = null

    private val _doesMoneyChange = MutableStateFlow(false)
    private val _doesMemoChange = MutableStateFlow(false)
    private val _doesCategoryChange = MutableStateFlow(false)
    private val _doesDateChange = MutableStateFlow(false)
    private val _forceHidden = MutableStateFlow(false)

    val isSubmitButtonEnable: Flow<Boolean> = combine(
        _doesMoneyChange,
        _doesMemoChange,
        _doesCategoryChange,
        _doesDateChange,
        _forceHidden
    )
    { money, memo, category, date, forcedHidden ->
        if (defaultTransaction == null) {
            return@combine false
        }
        if (forcedHidden) {
            return@combine false
        }
        Log.d(TAG, "money: $money ")
        Log.d(TAG, "memo: $memo ")
        Log.d(TAG, "category: $category ")
        Log.d(TAG, "date: $date ")
        Log.d(TAG, "forcedHidden: $forcedHidden ")
        Log.d(TAG, "defaultTransaction: $defaultTransaction ")
        return@combine money || memo || category || date
    }.distinctUntilChanged()

    fun onMoneyChange(newMoney: Double?) {
        _doesMoneyChange.value = defaultTransaction?.money != newMoney
    }

    fun onMemoChange(newMemo: String?) {
        _doesMemoChange.value = defaultTransaction?.memo ?: "" != newMemo ?: ""
    }

    fun onCategoryChange(categoryId: Int) {
        _doesCategoryChange.value = defaultTransaction?.cat_id != categoryId

    }

    fun onDateChange(newDate: Int) {
        _doesDateChange.value = defaultTransaction?.date != newDate

    }

    fun forcedHidden(shouldBeHidden: Boolean) {
        _forceHidden.value = shouldBeHidden
    }

    fun setDefaultTransaction(transaction: TransactionEntity) {
        defaultTransaction = transaction
    }

    private val TAG = "DetailEditTransactionVi"

}