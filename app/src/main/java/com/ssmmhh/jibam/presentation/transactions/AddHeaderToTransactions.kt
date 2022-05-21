package com.ssmmhh.jibam.presentation.transactions

import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.util.DateUtils.toMilliSeconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.*

suspend fun addHeaderToTransactions(currentList: List<TransactionDto>): List<TransactionsRecyclerViewItem> =
    withContext(Dispatchers.IO) {
        if (currentList.isEmpty()) {
            return@withContext listOf(TransactionsRecyclerViewItem.DatabaseIsEmpty)
        }
        val resultList = ArrayList<TransactionsRecyclerViewItem>()
        var headerDate = currentList[0].date
        var incomeSum = BigDecimal.ZERO
        var expensesSum = BigDecimal.ZERO
        val tempList = ArrayList<TransactionsRecyclerViewItem>()
        currentList.forEach { item ->
            if (item.date.isTheSameDayAs(headerDate)) {
                //make new header and items
                tempList.add(item.toTransactionsRecyclerViewItem())
                if (item.money >= BigDecimal.ZERO) { //income
                    incomeSum += item.money
                } else { //expenses
                    expensesSum += item.money
                }
            } else {
                //add header and items
                resultList.add(createHeader(headerDate, incomeSum, expensesSum, tempList.size))
                resultList.addAll(tempList)
                //clear item to defualt
                headerDate = item.date
                tempList.clear()
                incomeSum = BigDecimal.ZERO
                expensesSum = BigDecimal.ZERO
                //make new header and items
                tempList.add(item.toTransactionsRecyclerViewItem())
                if (item.money >= BigDecimal.ZERO) { //income
                    incomeSum += item.money
                } else { //expenses
                    expensesSum += item.money
                }
            }
        }
        //add header and items
        resultList.add(createHeader(headerDate, incomeSum, expensesSum, tempList.size))
        resultList.addAll(tempList)
        //clear item to defualt
        return@withContext resultList
    }


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
    //The calendar type (solar hijri or gregorian) does not matter here Used to indicate that
    // two date are in fact the same day.
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

