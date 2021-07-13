package com.example.jibi.ui.main.transaction

import com.example.jibi.util.mahdiLog
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MonthManger(val currentLocale: Locale) {
    private val fromDate = MutableStateFlow(getStartOfCurrentMonth())
    private val toDate = MutableStateFlow(getStartOfNextMonth())
    private val TAG = "MonthManger"

    fun getStartOfCurrentMonth(): Long {
        //TODO ADD SHAMSI SUPPORT

        return getStartOfCurrentMonthGeorgian()
    }

    fun getStartOfNextMonth(): Long {
        //TODO ADD SHAMSI SUPPORT
        return getStartOfNextMonthGeorgian()
    }

    fun getStartOfCurrentMonthGeorgian(
        timeStamp: Long = System.currentTimeMillis()
    ): Long {
        //get month and year for given timeStmap
        val currentDate = Date(timeStamp)
        val currentMonth = SimpleDateFormat("MM", currentLocale).format(currentDate)
        val currentYear = SimpleDateFormat("yyyy", currentLocale).format(currentDate)
        return getStartOfCurrentMonthGeorgian(currentMonth, currentYear)
    }

    fun getStartOfCurrentMonthGeorgian(
        currentMonth: String, currentYear: String
    ): Long {

        //final date string / i add 01 as day b/c it should be the first day of month aka start of month
        val strDate = "01-$currentMonth-$currentYear"
        //convert string date into date then unix timeStamp
        val formatter: DateFormat = SimpleDateFormat("dd-MM-yyyy", currentLocale)
        val resultDate = formatter.parse(strDate) as Date
        //some log for debug later
        mahdiLog(
            className = TAG,
            message = "getStartOfCurrentMonth result is " + SimpleDateFormat(
                "yyyy-MM-dd  HH:mm:ss", currentLocale
            ).format(
                resultDate
            ) + "\n unix time: ${resultDate.time}"
        )
        return resultDate.time
    }

    fun getStartOfNextMonthGeorgian(
        timeStamp: Long = System.currentTimeMillis()
    ): Long {
        //get the month and year of given timeStamp
        val currentDate = Date(timeStamp)
        val currentMonth = SimpleDateFormat("MM", currentLocale).format(currentDate)
        val currentYear = SimpleDateFormat("yyyy", currentLocale).format(currentDate)
        return getStartOfNextMonthGeorgian(currentMonth, currentYear)
    }

    fun getStartOfNextMonthGeorgian(
        currentMonth: String, currentYear: String
    ): Long {

        //change month
        var nextMonth = currentMonth.toInt().plus(1)
        var year = currentYear.toInt()
        //check for make sure nextMonth would'nt be 13
        if (nextMonth > 12) {
            nextMonth = 1
            year = year.plus(1)
        }

        val nextMonthStr = if (nextMonth > 9) {
            nextMonth
        } else {
            "0$nextMonth"
        }
        //final string date
        val strDate = "01-$nextMonthStr-$year"
        //convert string date to date then unix timestamp
        val formatter: DateFormat = SimpleDateFormat("dd-MM-yyyy", currentLocale)
        val resultDate = formatter.parse(strDate) as Date
        //debug log
        mahdiLog(
            className = TAG,
            message = "getStartOfNextMonth result is " + SimpleDateFormat(
                "yyyy-MM-dd  HH:mm:ss", currentLocale
            ).format(
                resultDate
            ) + "\n unix time: ${resultDate.time}"
        )
        return resultDate.time
    }
}