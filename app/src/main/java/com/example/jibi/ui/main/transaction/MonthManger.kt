package com.example.jibi.ui.main.transaction

import com.example.jibi.util.DateUtils
import com.example.jibi.util.SolarCalendar
import com.example.jibi.util.isFarsi
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.*

class MonthManger(val currentLocale: Locale) {

    private val fromDate = MutableStateFlow(getStartOfCurrentMonth())
    private val toDate = MutableStateFlow(getEndOfCurrentMonth())
    private val TAG = "MonthManger"

    fun getStartOfCurrentMonth(): Long = if (currentLocale.isFarsi()) {
        getStartOfCurrentMonthShamsi()
    } else {
        getStartOfCurrentMonthGeorgian()
    }


    fun getEndOfCurrentMonth(): Long = if (currentLocale.isFarsi()) {
        getEndOfCurrentMonthShamsi()
    } else {
        getEndOfCurrentMonthGeorgian()
    }

    fun getStartOfCurrentMonthShamsi(
        currentMonth: Int, currentYear: Int
    ): Long {
        return DateUtils.shamsiToUnixTimeStamp(
            jy = currentYear,
            jm = currentMonth,
            jd = 1
        )
    }


    fun getEndOfCurrentMonthShamsi(
        currentMonth: Int, currentYear: Int
    ): Long {
        var year = currentYear
        var month = currentMonth.plus(1)

        if (month > 12) {
            month = 1
            year = currentYear.plus(1)
        }
        return DateUtils.shamsiToUnixTimeStamp(
            jy = year,
            jm = month,
            jd = 1
        )
    }

    fun getStartOfCurrentMonthGeorgian(
        currentMonth: Int, currentYear: Int
    ): Long = DateUtils.gregorianToUnixTimestamp(year = currentYear, month = currentMonth, 1)


    fun getEndOfCurrentMonthGeorgian(
        currentMonth: Int, currentYear: Int
    ): Long {
        var year = currentYear
        var month = currentMonth.plus(1)

        if (month > 12) {
            month = 1
            year = currentYear.plus(1)
        }
        return DateUtils.gregorianToUnixTimestamp(
            year = year,
            month = month,
            day = 1
        )
    }

    //utils
    fun getEndOfCurrentMonthGeorgian(
        timeStamp: Long = System.currentTimeMillis()
    ): Long {
        //get the month and year of given timeStamp
        val currentDate = Date(timeStamp)
        val currentMonth = SimpleDateFormat("MM", currentLocale).format(currentDate)
        val currentYear = SimpleDateFormat("yyyy", currentLocale).format(currentDate)
        return getEndOfCurrentMonthGeorgian(currentMonth.toInt(), currentYear.toInt())
    }

    fun getStartOfCurrentMonthGeorgian(
        timeStamp: Long = System.currentTimeMillis()
    ): Long {
        //get month and year for given timeStamp
        val currentDate = Date(timeStamp)
        val currentMonth = SimpleDateFormat("MM", currentLocale).format(currentDate)
        val currentYear = SimpleDateFormat("yyyy", currentLocale).format(currentDate)
        return getStartOfCurrentMonthGeorgian(currentMonth.toInt(), currentYear.toInt())
    }

    fun getEndOfCurrentMonthShamsi(
        timeStamp: Long = System.currentTimeMillis()
    ): Long {
        //get month and year for given timeStamp
        val shamsiDate = SolarCalendar.calcSolarCalendar(
            timeStamp,
            SolarCalendar.ShamsiPatterns.YEAR_MONTH,
            currentLocale
        )
        val currentYear = shamsiDate.substring(0, shamsiDate.indexOf('_'))
        val currentMonth = shamsiDate.substring(shamsiDate.indexOf('_').plus(1))
        return getEndOfCurrentMonthShamsi(currentMonth.toInt(), currentYear.toInt())
    }

    fun getStartOfCurrentMonthShamsi(
        timeStamp: Long = System.currentTimeMillis()
    ): Long {
        //get month and year for given timeStamp
        val shamsiDate = SolarCalendar.calcSolarCalendar(
            timeStamp,
            SolarCalendar.ShamsiPatterns.YEAR_MONTH,
            currentLocale
        )
        val currentYear = shamsiDate.substring(0, shamsiDate.indexOf('_'))
        val currentMonth = shamsiDate.substring(shamsiDate.indexOf('_').plus(1))
        return getStartOfCurrentMonthShamsi(currentMonth.toInt(), currentYear.toInt())
    }
}