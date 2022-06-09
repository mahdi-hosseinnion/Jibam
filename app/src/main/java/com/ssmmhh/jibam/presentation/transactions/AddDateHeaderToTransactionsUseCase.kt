package com.ssmmhh.jibam.presentation.transactions

import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.util.DateUtils.toMilliSeconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.util.*
import javax.inject.Inject


class AddDateHeaderToTransactionsUseCase
constructor(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    @Inject
    constructor() : this(Dispatchers.Default)

    suspend fun invoke(
        currentList: List<TransactionDto>,
        searchQuery: String
    ): List<TransactionsRecyclerViewItem> =
        withContext(coroutineDispatcher) {
            if (currentList.isEmpty()) {
                if (searchQuery.isNotEmpty()) {
                    return@withContext listOf(TransactionsRecyclerViewItem.NoResultFound)
                }
                return@withContext listOf(TransactionsRecyclerViewItem.DatabaseIsEmpty)
            }
            return@withContext buildList {
                var headerDate = currentList[0].date
                var headerIncomeSum = ZERO
                var headerExpensesSum = ZERO
                val headerTransactions = ArrayList<TransactionsRecyclerViewItem>()
                currentList.forEach { item ->
                    if (item.date.isTheSameDayAs(headerDate)) {
                        headerTransactions.add(item.toTransactionsRecyclerViewItem())
                        if (item.money >= ZERO) { //income
                            headerIncomeSum += item.money
                        } else { //expenses
                            headerExpensesSum += item.money
                        }
                    } else {
                        //add header and items
                        add(
                            createHeader(
                                headerDate,
                                headerIncomeSum,
                                headerExpensesSum,
                                headerTransactions.size
                            )
                        )
                        addAll(headerTransactions)
                        //reset header values.
                        headerDate = item.date
                        if (item.money >= ZERO) { //income
                            headerIncomeSum = item.money
                            headerExpensesSum = ZERO
                        } else { //expenses
                            headerIncomeSum = ZERO
                            headerExpensesSum = item.money
                        }
                        headerTransactions.clear()
                        headerTransactions.add(item.toTransactionsRecyclerViewItem())
                    }
                }
                //add last header
                add(
                    createHeader(
                        headerDate,
                        headerIncomeSum,
                        headerExpensesSum,
                        headerTransactions.size
                    )
                )
                addAll(headerTransactions)
            }
        }


    //TODO do not show sums if there is only one expenses and income.
    private fun createHeader(
        date: Long,
        income: BigDecimal,
        expenses: BigDecimal,
        length: Int
    ): TransactionsRecyclerViewItem.Header {
        return if (length > 1) {
            TransactionsRecyclerViewItem.Header(
                expensesSum = expenses,
                incomeSum = income,
                date = date
            )
        } else {
            TransactionsRecyclerViewItem.Header(
                expensesSum = null,
                incomeSum = null,
                date = date
            )
        }
    }

    private fun Long.isTheSameDayAs(other: Long): Boolean {
        //The calendar type (solar hijri or gregorian) does not matter here.
        //GregorianCalendar time zone by default is the device time zone.
        val thisCalendar = GregorianCalendar().apply {
            timeInMillis = this@isTheSameDayAs.toMilliSeconds()
        }
        val otherCalendar = GregorianCalendar().apply {
            timeInMillis = other.toMilliSeconds()
        }
        return ((thisCalendar.get(Calendar.DATE) == otherCalendar.get(Calendar.DATE)) &&
                (thisCalendar.get(Calendar.MONTH) == otherCalendar.get(Calendar.MONTH)) &&
                (thisCalendar.get(Calendar.YEAR) == otherCalendar.get(Calendar.YEAR)))

    }
}

