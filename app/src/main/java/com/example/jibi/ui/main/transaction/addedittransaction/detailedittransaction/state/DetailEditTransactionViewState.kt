package com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state

import com.example.jibi.models.TransactionEntity
import com.example.jibi.util.StateEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.util.*

data class DetailEditTransactionViewState(
    private val combineCalender: GregorianCalendar? = null,
)

class SubmitButtonState(private val defaultTransaction: TransactionEntity) {

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
        return@combine money || memo || category || date
    }

    fun onMoneyChange(newMoney: Double?) {
        _doesMoneyChange.value = defaultTransaction.money != newMoney
    }

    fun onMemoChange(newMemo: String?) {
        _doesMoneyChange.value = defaultTransaction.memo != newMemo
    }

    fun onCategoryChange(categoryId: Int) {
        _doesMoneyChange.value = defaultTransaction.cat_id != categoryId

    }

    fun onDateChange(newDate: Int) {
        _doesMoneyChange.value = defaultTransaction.date != newDate

    }
}