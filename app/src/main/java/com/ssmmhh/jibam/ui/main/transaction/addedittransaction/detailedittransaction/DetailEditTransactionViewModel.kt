package com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction

import android.content.res.Resources
import android.util.Log
import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.models.Transaction
import com.ssmmhh.jibam.models.TransactionEntity
import com.ssmmhh.jibam.models.mappers.toTransactionEntity
import com.ssmmhh.jibam.repository.cateogry.CategoryRepository
import com.ssmmhh.jibam.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionPresenterState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.SubmitButtonState
import com.ssmmhh.jibam.ui.main.transaction.common.BaseViewModel
import com.ssmmhh.jibam.util.*
import com.ssmmhh.jibam.util.Constants.EXPENSES_TYPE_MARKER
import com.ssmmhh.jibam.util.Constants.INCOME_TYPE_MARKER
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class DetailEditTransactionViewModel
@Inject
constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val currentLocale: Locale
) : BaseViewModel<DetailEditTransactionViewState, DetailEditTransactionStateEvent>() {

    val submitButtonState = SubmitButtonState()

    init {
        //retrieve all of categories from cache
        getAllOfCategories()
        //clear state when navigate
        setPresenterState(DetailEditTransactionPresenterState.NoneState)
    }

    override fun initNewViewState(): DetailEditTransactionViewState =
        DetailEditTransactionViewState()

    override suspend fun getResultByStateEvent(
        stateEvent: DetailEditTransactionStateEvent
    ): DataState<DetailEditTransactionViewState> = when (stateEvent) {
        is DetailEditTransactionStateEvent.GetTransactionById -> transactionRepository.getTransactionById(
            stateEvent
        )
        is DetailEditTransactionStateEvent.GetAllOfCategories -> categoryRepository.getAllOfCategories(
            stateEvent
        )
        is DetailEditTransactionStateEvent.UpdateTransaction -> transactionRepository.updateTransaction(
            stateEvent
        )
        is DetailEditTransactionStateEvent.DeleteTransaction -> transactionRepository.deleteTransaction(
            stateEvent
        )
    }

    override fun updateViewState(newViewState: DetailEditTransactionViewState): DetailEditTransactionViewState {
        val outdated = getCurrentViewStateOrNew()

        val defaultTransaction = newViewState.defaultTransaction ?: outdated.defaultTransaction

        val transactionCategoryType: Int? =
            newViewState.transactionCategoryType ?: outdated.transactionCategoryType
            ?: defaultTransaction?.let {
                if (it.money > 0) {
                    INCOME_TYPE_MARKER
                } else {
                    EXPENSES_TYPE_MARKER
                }
            }
        //money base value should be default transaction money
        val moneyStr =
            newViewState.moneyStr ?: outdated.moneyStr ?: convertTransactionMoneyToDoubleMoney(
                defaultTransaction?.money
            )

        if (defaultTransaction != null) {
            submitButtonState.setDefaultTransaction(defaultTransaction.toTransactionEntity())
        }
        return DetailEditTransactionViewState(
            defaultTransaction = defaultTransaction,
            moneyStr = moneyStr,
            transaction = newViewState.transaction ?: outdated.transaction ?: defaultTransaction,
            transactionCategoryType = transactionCategoryType,
            combineCalender = newViewState.combineCalender ?: outdated.combineCalender,
            allOfCategories = newViewState.allOfCategories ?: outdated.allOfCategories,
            presenterState = newViewState.presenterState ?: outdated.presenterState,
            successfullyDeletedTransactionIndicator = newViewState.successfullyDeletedTransactionIndicator
                ?: outdated.successfullyDeletedTransactionIndicator,
        )
    }

    private fun convertTransactionMoneyToDoubleMoney(value: Double?): String? {
        if (value == null) {
            return null
        }
        val transactionMoney = if (value > 0)
            value.toString()
        else if (value < 0)
            (value.times(-1)).toString()
        else "0"
        return convertDoubleToString(transactionMoney)
    }

    fun getTransactionById(transactionId: Int) {
        launchNewJob(
            DetailEditTransactionStateEvent.GetTransactionById(transactionId)
        )
    }

    private fun getAllOfCategories() {
        launchNewJob(
            DetailEditTransactionStateEvent.GetAllOfCategories
        )
    }

    fun setPresenterState(newState: DetailEditTransactionPresenterState) {
        setViewState(
            DetailEditTransactionViewState(
                presenterState = Event(newState)
            )
        )
    }

    fun getTransactionCategoryId(): Int? = viewState.value?.transaction?.categoryId

    fun getTransactionCategoryType(): Int? = viewState.value?.transactionCategoryType

    fun getDefaultTransaction(): Transaction? = viewState.value?.defaultTransaction

    fun getTransaction(): Transaction? = viewState.value?.transaction

    fun getCombineCalender(): GregorianCalendar {
        val viewStateValue = viewState.value?.combineCalender

        if (viewStateValue != null) {
            return viewStateValue
        }
        val transaction = getTransaction()

        val newCalender = GregorianCalendar(currentLocale)

        if (transaction != null) {
            newCalender.timeInMillis = ((transaction.date.toLong()).times(1_000))
        } else {
            Log.e(TAG, "getCombineCalender: combineCalender and transaction is null")
        }

        return newCalender

    }

    fun setToCombineCalender(field: Int, value: Int) {
        val updated = getCombineCalender()
        updated.set(field, value)
        setViewState(
            DetailEditTransactionViewState(
                combineCalender = updated
            )
        )

        val newDate = (updated.timeInMillis.div(1_000)).toInt()

        val outDatedTransaction = getTransaction() ?: getDefaultTransaction()
        setViewState(
            DetailEditTransactionViewState(
                transaction = outDatedTransaction?.copy(
                    date = newDate
                )
            )
        )
        submitButtonState.onDateChange(newDate)

    }

    fun setToCombineCalender(year: Int, month: Int, day: Int) {
        val updated = getCombineCalender()
        updated.set(year, month, day)
        setViewState(
            DetailEditTransactionViewState(
                combineCalender = updated
            )
        )
        val newDate = (updated.timeInMillis.div(1_000)).toInt()
        val outDatedTransaction = getTransaction() ?: getDefaultTransaction()
        setViewState(
            DetailEditTransactionViewState(
                transaction = outDatedTransaction?.copy(
                    date = newDate
                )
            )
        )
        submitButtonState.onDateChange(newDate)
    }

    fun setTransactionCategory(category: Category) {
        val transaction = getTransaction()
        setViewState(
            DetailEditTransactionViewState(
                transaction = transaction?.copy(
                    categoryId = category.id,
                    categoryImage = category.img_res,
                    categoryName = category.name
                ),
                transactionCategoryType = category.type
            )
        )
        submitButtonState.onCategoryChange(categoryId = category.id)

    }

    fun deleteTransaction(transactionId: Int) {
        launchNewJob(
            DetailEditTransactionStateEvent.DeleteTransaction(
                transactionId = transactionId,
                showSuccessToast = true
            )
        )
    }

    fun onMemoChanged(memo: String) {
        val outDatedTransaction = getTransaction() ?: getDefaultTransaction()
        setViewState(
            DetailEditTransactionViewState(
                transaction = outDatedTransaction?.copy(
                    memo = memo
                )
            )
        )
        submitButtonState.onMemoChange(memo)
    }

    fun onMoneyChanged(money: String, resources: Resources) {

        setViewState(
            DetailEditTransactionViewState(
                moneyStr = money
            )
        )
        //we show user the inserted money in their language digits not in english so we need to convert it here
        submitButtonState.onMoneyChange((money.convertLocaleNumberToEnglish(resources)).toDoubleOrNull())
    }

    fun updateTransaction(entity: TransactionEntity) {
        launchNewJob(
            DetailEditTransactionStateEvent.UpdateTransaction(entity)
        )
    }


    companion object {
        private const val TAG = "DetailEditTransactionVi"
    }

}