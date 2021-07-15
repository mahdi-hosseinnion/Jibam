package com.example.jibi.ui.main.transaction

import com.example.jibi.models.Month
import com.example.jibi.util.DateUtils
import com.example.jibi.util.SolarCalendar
import com.example.jibi.util.isFarsi
import com.example.jibi.util.mahdiLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonthManger
@Inject
constructor(private val currentLocale: Locale) {
    private val TAG = "MonthManger"

    private val _fromDate = MutableStateFlow(getStartOfCurrentMonth())
    private val _toDate = MutableStateFlow(getEndOfCurrentMonth())

    val currentMonth: Flow<Month> = combine(
        _fromDate,
        _toDate
    ) { from, to ->
        return@combine Month(
            from.longDateToIntDate(),
            to.longDateToIntDate(),
            getMonthName(from)
        )
    }

    private fun Long.longDateToIntDate(): Int = (this.div(1_000)).toInt()

    private fun getStartOfCurrentMonth(): Long = if (currentLocale.isFarsi()) {
        getStartOfCurrentMonthShamsi()
    } else {
        getStartOfCurrentMonthGeorgian()
    }


    private fun getEndOfCurrentMonth(): Long = if (currentLocale.isFarsi()) {
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

    private fun getMonthName(timeStamp: Long): String {
        val name = if (currentLocale.isFarsi()) {
            getShamsiMonthName(timeStamp)
        } else {
            getGeorgianMonthName(timeStamp)
        }
        mahdiLog(TAG, "NAME IS" + name)
        return name
    }

    private fun getShamsiMonthName(timeStamp: Long): String =
        SolarCalendar.calcSolarCalendar(
            timeStamp,
            SolarCalendar.ShamsiPatterns.JUST_MONTH_NAME,
            currentLocale
        )


    private fun getGeorgianMonthName(timeStamp: Long): String {
        //get the month and year of given timeStamp
        val currentDate = Date(timeStamp)
        return SimpleDateFormat("MMM", currentLocale).format(currentDate)
    }
}