package com.example.jibi.ui.main.transaction.transactions

import android.content.res.Resources
import com.example.jibi.models.Transaction
import com.example.jibi.ui.main.transaction.transactions.TransactionsListAdapter.Companion.NO_RESULT_FOUND_IN_DATABASE
import com.example.jibi.util.SolarCalendar
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

    fun addHeaderToTransactions(currentList: List<Transaction>): List<Transaction> {
        if (currentList.isEmpty()) {
            return listOf(NO_RESULT_FOUND_IN_DATABASE)
        }
        val resultList = ArrayList<Transaction>()
        var headerDate = currentDateInString(currentList[0].date)
        var incomeSum = 0.0
        var expensesSum = 0.0
        var tempList = ArrayList<Transaction>()
        for (item in currentList) {
            if (currentDateInString(item.date) == headerDate) {
                //make new header and items
                tempList.add(item)
                if (item.money >= 0) { //income
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
                incomeSum = 0.0
                expensesSum = 0.0
                //make new header and items
                tempList.add(item)
                if (item.money >= 0) { //income
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

    private fun currentDateInString(time: Int): String {
        val dv: Long = ((time.toLong()) * 1000) // its need to be in milisecond

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
        income: Double,
        expenses: Double,
        length: Int
    ): Transaction {
        return if (length > 1) {
            Transaction(
                id = TransactionsListAdapter.HEADER_ITEM,
                money = expenses,
                incomeSum = income,
                memo = date
            )
        } else {
            Transaction(
                id = TransactionsListAdapter.HEADER_ITEM,
                money = 0.0,
                incomeSum = 0.0,
                memo = date,
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