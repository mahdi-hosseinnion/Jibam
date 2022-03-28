package com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction

import android.content.res.Resources
import android.util.Log
import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.persistence.dtos.TransactionDto
import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.repository.cateogry.CategoryRepository
import com.ssmmhh.jibam.repository.tranasction.TransactionRepository
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionPresenterState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionStateEvent
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionViewState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.SubmitButtonState
import com.ssmmhh.jibam.ui.main.transaction.common.BaseViewModel
import com.ssmmhh.jibam.util.*
import com.ssmmhh.jibam.persistence.entities.CategoryEntity.Companion.EXPENSES_TYPE_MARKER
import com.ssmmhh.jibam.persistence.entities.CategoryEntity.Companion.INCOME_TYPE_MARKER
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.math.BigDecimal
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
        is DetailEditTransactionStateEvent.GetAllOfCategories -> {
            val result = categoryRepository.getAllOfCategories(
                stateEvent
            )
            DataState(
                stateMessage = result.stateMessage,
                data = DetailEditTransactionViewState(allOfCategories = result.data?.allOfCategories),
                stateEvent = result.stateEvent
            )
        }
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
                if (it.money > BigDecimal.ZERO) {
                    INCOME_TYPE_MARKER
                } else {
                    EXPENSES_TYPE_MARKER
                }
            }
        //money base value should be default transaction money
        val moneyStr =
            newViewState.moneyStr ?: outdated.moneyStr ?: convertTransactionMoneyToBigDecimalMoney(
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

    private fun convertTransactionMoneyToBigDecimalMoney(value: BigDecimal?): String? {
        if (value == null) {
            return null
        }
        val transactionMoney = if (value > BigDecimal.ZERO)
            value.toString()
        else if (value < BigDecimal.ZERO)
            (value.times(BigDecimal("-1"))).toPlainString()
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

    fun getDefaultTransaction(): TransactionDto? = viewState.value?.defaultTransaction

    fun getTransaction(): TransactionDto? = viewState.value?.transaction

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

        val newDate = (updated.timeInMillis.div(1_000))

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
        val newDate = (updated.timeInMillis.div(1_000))
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

    fun setTransactionCategory(categoryEntity: Category) {
        val transaction = getTransaction()
        setViewState(
            DetailEditTransactionViewState(
                transaction = transaction?.copy(
                    categoryId = categoryEntity.id,
                    categoryImageResourceName = categoryEntity.image.resourceName,
                    categoryImageBackgroundColor = categoryEntity.image.backgroundColor,
                    categoryName = categoryEntity.name
                ),
                transactionCategoryType = categoryEntity.type
            )
        )
        submitButtonState.onCategoryChange(categoryId = categoryEntity.id)

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
        submitButtonState.onMoneyChange((money.convertLocaleNumberToEnglish(resources)).toBigDecimalOrNull())
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
