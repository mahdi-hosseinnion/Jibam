package com.ssmmhh.jibam.presentation.transactions

import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.util.DateUtils
import java.math.BigDecimal


fun addHeaderToTransactions(currentList: List<TransactionDto>): List<TransactionsRecyclerViewItem> {
    if (currentList.isEmpty()) {
        return listOf(TransactionsRecyclerViewItem.DatabaseIsEmpty)
    }
    val resultList = ArrayList<TransactionsRecyclerViewItem>()
    var headerDate = currentList[0].date
    var incomeSum = BigDecimal.ZERO
    var expensesSum = BigDecimal.ZERO
    val tempList = ArrayList<TransactionsRecyclerViewItem>()
    currentList.forEach { item ->
        /**
         * There is serious here.
         * When there is differnt time zones like
         * [1652716786,1652716772,1652643609,1652630378]
         * These are all the same day in Iran day time but different day in GMT.
         */
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
    return resultList
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

private fun Long.isTheSameDayAs(other: Long): Boolean =
    (this.div(DateUtils.DAY_IN_SECONDS)) == other.div(DateUtils.DAY_IN_SECONDS)

