package com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state

import com.example.jibi.models.Category
import com.example.jibi.models.Transaction
import com.example.jibi.models.TransactionEntity
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionPresenterState
import com.example.jibi.util.StateEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.util.*

data class DetailEditTransactionViewState(
    // default transaction used to determine if user update transaction or did not
    val defaultTransaction: Transaction? = null,
    // transaction used to maintain inserted data during configuration change
    val transaction: Transaction? = null,
    val transactionCategoryType: Int? = null,
    val combineCalender: GregorianCalendar? = null,
    val allOfCategories: List<Category>? = null,
    val successfullyDeletedTransactionIndicator: Int? = null,
    val presenterState: DetailEditTransactionPresenterState? = null

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

    val isSubmitButtonEnable: Flow<Boolean> = combine(
        _doesMoneyChange,
        _doesMemoChange,
        _doesCategoryChange,
        _doesDateChange
    )
    { money, memo, category, date ->
        if (defaultTransaction == null) {
            return@combine false
        }
        return@combine money || memo || category || date
    }

    fun onMoneyChange(newMoney: Double?) {
        _doesMoneyChange.value = defaultTransaction?.money != newMoney
    }

    fun onMemoChange(newMemo: String?) {
        _doesMoneyChange.value = defaultTransaction?.memo != newMemo
    }

    fun onCategoryChange(categoryId: Int) {
        _doesMoneyChange.value = defaultTransaction?.cat_id != categoryId

    }

    fun onDateChange(newDate: Int) {
        _doesMoneyChange.value = defaultTransaction?.date != newDate

    }

    fun setDefaultTransaction(transaction: TransactionEntity) {
        defaultTransaction = transaction
    }

}