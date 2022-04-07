package com.ssmmhh.jibam.presentation.transactions

import com.ssmmhh.jibam.data.model.TransactionsRecyclerViewItem
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.util.DateHolderWithWeekDay
import com.ssmmhh.jibam.util.unixTimeStampToShamsiDate
import java.math.BigDecimal
import java.util.*

class AddHeaderToTransactions(
    private val isSolarCalendar: Boolean

) {

    fun addHeaderToTransactions(currentList: List<TransactionDto>): List<TransactionsRecyclerViewItem> {
        if (currentList.isEmpty()) {
            return listOf(TransactionsRecyclerViewItem.DatabaseIsEmpty)
        }
        val resultList = ArrayList<TransactionsRecyclerViewItem>()
        var headerDate = currentList[0].date
        var incomeSum = BigDecimal.ZERO
        var expensesSum = BigDecimal.ZERO
        var tempList = ArrayList<TransactionsRecyclerViewItem>()
        for (item in currentList) {
            if (item.date == headerDate) {
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

}