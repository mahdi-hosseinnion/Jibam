package com.ssmmhh.jibam.presentation.addedittransaction.detailedittransaction.state

import android.util.Log
import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.util.Event
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.util.*

data class DetailEditTransactionViewState(
    // default transaction used to determine if user update transaction or did not
    val defaultTransaction: TransactionDto? = null,
    // transaction used to maintain inserted data during configuration change
    val transaction: TransactionDto? = null,
    val moneyStr: String? = null,
    val transactionCategoryType: Int? = null,
    val combineCalender: GregorianCalendar? = null,
    val allOfCategories: Event<List<Category>?>? = null,
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

    fun onMoneyChange(newMoney: BigDecimal?) {
        //default money is negative when transaction is in expenses category but newMoney is
        //always positive but so we use .absoluteValue
        _doesMoneyChange.value =
            defaultTransaction?.money?.abs() != newMoney?.abs()
    }

    fun onMemoChange(newMemo: String?) {
        _doesMemoChange.value = defaultTransaction?.memo ?: "" != newMemo ?: ""
    }

    fun onCategoryChange(categoryId: Int) {
        _doesCategoryChange.value = defaultTransaction?.cat_id != categoryId

    }

    fun onDateChange(newDate: Long) {
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