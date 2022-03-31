package com.ssmmhh.jibam.presentation.feature_transactions

import android.content.res.Resources
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.data.model.TransactionsRecyclerViewItem
import com.ssmmhh.jibam.util.SolarCalendar
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddHeaderToTransactions(
    private val currentLocale: Locale,
    private val resources: Resources,
    private val isSolarCalendar: Boolean

) {
    val today: String =
        getFormattedDate(System.currentTimeMillis())
    val yesterday: String = getFormattedDate(System.currentTimeMillis().minus(86_400_000L))

    fun addHeaderToTransactions(currentList: List<TransactionDto>): List<TransactionsRecyclerViewItem> {
        if (currentList.isEmpty()) {
            return listOf(TransactionsRecyclerViewItem.DatabaseIsEmpty)
        }
        val resultList = ArrayList<TransactionsRecyclerViewItem>()
        var headerDate = currentDateInString(currentList[0].date)
        var incomeSum = BigDecimal.ZERO
        var expensesSum = BigDecimal.ZERO
        var tempList = ArrayList<TransactionsRecyclerViewItem>()
        for (item in currentList) {
            if (currentDateInString(item.date) == headerDate) {
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
                headerDate = currentDateInString(item.date)
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

    private fun currentDateInString(time: Long): String {
        val dv: Long = (time * 1000) // its need to be in milisecond

        val transDate = getFormattedDate(dv)

        if (transDate == today) {
            return TransactionsListAdapter.TODAY
        }
        if (transDate == yesterday) {
            return TransactionsListAdapter.YESTERDAY
        }
        return transDate
    }

    private fun createHeader(
        date: String,
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

    private fun getFormattedDate(unixTimeStamp: Long): String {
        val df: Date = Date(unixTimeStamp)

        return if (isSolarCalendar) {
            SolarCalendar.calcSolarCalendar(
                df,
                SolarCalendar.ShamsiPatterns.RECYCLER_VIEW,
                resources,
                currentLocale
            )
        } else {
            SimpleDateFormat(
                TransactionsListAdapter.HEADER_DATE_PATTERN,
                currentLocale
            ).format(df)
        }

    }
}