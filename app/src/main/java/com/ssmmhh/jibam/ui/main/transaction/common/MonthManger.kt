package com.ssmmhh.jibam.ui.main.transaction.common

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.fragment.app.FragmentManager
import com.ssmmhh.jibam.models.Month
import com.ssmmhh.jibam.util.*
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
constructor(
    private val currentLocale: Locale,
    private val _resources: Resources,
    private val sharedPreferences: SharedPreferences
) {
    private val TAG = "MonthManger"

    private val _fromDate = MutableStateFlow(
        getStartOfCurrentMonth(
            DateUtils.getCurrentUnixTimeInMilliSeconds()
        )
    )
    private val _toDate = MutableStateFlow(
        getEndOfCurrentMonth(
            DateUtils.getCurrentUnixTimeInMilliSeconds()
        )
    )

    val currentMonth: Flow<Month> = combine(
        _fromDate,
        _toDate
    ) { from, to ->
        return@combine Month(
            from.toSecond(),
            to.toSecond(),
            getMonthName(from)
        )
    }

    private fun Long.toSecond(): Long = (this.div(1_000))

    fun setMonthAndYear(month: Int, year: Int) {
        val from = getStartOfCurrentMonth(month, year)
        val to = getEndOfCurrentMonth(month, year)
        _toDate.value = to
        _fromDate.value = from
    }

    fun getStartOfCurrentMonth(
        timeStamp: Long
    ): Long = if (sharedPreferences.isCalendarSolar(currentLocale)) {
        getStartOfCurrentMonthShamsi(timeStamp)
    } else {
        getStartOfCurrentMonthGeorgian(timeStamp)
    }


    fun getEndOfCurrentMonth(
        timeStamp: Long
    ): Long = if (sharedPreferences.isCalendarSolar(currentLocale)) {
        getEndOfCurrentMonthShamsi(timeStamp)
    } else {
        getEndOfCurrentMonthGeorgian(timeStamp)
    }

    private fun getStartOfCurrentMonth(
        currentMonth: Int, currentYear: Int
    ): Long = if (sharedPreferences.isCalendarSolar(currentLocale)) {
        getStartOfCurrentMonthShamsi(currentMonth, currentYear)
    } else {
        getStartOfCurrentMonthGeorgian(currentMonth, currentYear)
    }


    private fun getEndOfCurrentMonth(
        currentMonth: Int, currentYear: Int
    ): Long = if (sharedPreferences.isCalendarSolar(currentLocale)) {
        getEndOfCurrentMonthShamsi(currentMonth, currentYear)
    } else {
        getEndOfCurrentMonthGeorgian(currentMonth, currentYear)
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
        timeStamp: Long
    ): Long {
        //get the month and year of given timeStamp
        val currentDate = Date(timeStamp)
        val currentMonth = SimpleDateFormat("MM", currentLocale).format(currentDate)
        val currentYear = SimpleDateFormat("yyyy", currentLocale).format(currentDate)
        return getEndOfCurrentMonthGeorgian(currentMonth.toInt(), currentYear.toInt())
    }

    fun getStartOfCurrentMonthGeorgian(
        timeStamp: Long
    ): Long {
        //get month and year for given timeStamp
        val currentDate = Date(timeStamp)
        val currentMonth = SimpleDateFormat("MM", currentLocale).format(currentDate)
        val currentYear = SimpleDateFormat("yyyy", currentLocale).format(currentDate)
        return getStartOfCurrentMonthGeorgian(currentMonth.toInt(), currentYear.toInt())
    }

    fun getEndOfCurrentMonthShamsi(
        timeStamp: Long
    ): Long {
        //get month and year for given timeStamp
        val shamsiDate = SolarCalendar.calcSolarCalendar(
            timeStamp,
            SolarCalendar.ShamsiPatterns.YEAR_MONTH,
            _resources,
            currentLocale
        )
        val currentYear = shamsiDate.substring(0, shamsiDate.indexOf('_'))
        val currentMonth = shamsiDate.substring(shamsiDate.indexOf('_').plus(1))
        return getEndOfCurrentMonthShamsi(currentMonth.toInt(), currentYear.toInt())
    }

    fun getStartOfCurrentMonthShamsi(
        timeStamp: Long
    ): Long {
        //get month and year for given timeStamp
        val shamsiDate = SolarCalendar.calcSolarCalendar(
            timeStamp,
            SolarCalendar.ShamsiPatterns.YEAR_MONTH,
            _resources,
            currentLocale
        )
        val currentYear = shamsiDate.substring(0, shamsiDate.indexOf('_'))
        val currentMonth = shamsiDate.substring(shamsiDate.indexOf('_').plus(1))
        return getStartOfCurrentMonthShamsi(currentMonth.toInt(), currentYear.toInt())
    }

    fun getMonthName(timeStamp: Long = _fromDate.value): String {
        val name = if (sharedPreferences.isCalendarSolar(currentLocale)) {
            getShamsiMonthName(timeStamp)
        } else {
            getGeorgianMonthName(timeStamp)
        }
        return if (getYear() != getYear(System.currentTimeMillis())) {
            //if month is not in this year
            name + "\n" + getYear().toString().localizeNumber(_resources)
        } else name
    }

    private fun getShamsiMonthName(timeStamp: Long): String =
        SolarCalendar.calcSolarCalendar(
            timeStamp,
            SolarCalendar.ShamsiPatterns.JUST_MONTH_NAME,
            _resources,
            currentLocale
        )


    private fun getGeorgianMonthName(timeStamp: Long): String {
        //get the month and year of given timeStamp
        val currentDate = Date(timeStamp)
        return SimpleDateFormat("MMM", currentLocale).format(currentDate)
    }

    fun getCurrentMonth(): Int = getMonth(DateUtils.getCurrentUnixTimeInMilliSeconds())

    fun getMonth(timeStamp: Long = _fromDate.value): Int {
        return if (sharedPreferences.isCalendarSolar(currentLocale)) {
            SolarCalendar.calcSolarCalendar(
                timeStamp,
                SolarCalendar.ShamsiPatterns.JUST_MONTH_NUMBER,
                _resources,
                currentLocale
            ).toInt()
        } else {
            val currentDate = Date(timeStamp)
            return (SimpleDateFormat("M", currentLocale).format(currentDate)).toInt()
        }
    }

    fun getCurrentYear(): Int = getYear(DateUtils.getCurrentUnixTimeInMilliSeconds())

    fun getYear(timeStamp: Long = _fromDate.value): Int {
        return if (sharedPreferences.isCalendarSolar(currentLocale)) {
            SolarCalendar.calcSolarCalendar(
                timeStamp,
                SolarCalendar.ShamsiPatterns.JUST_YEAR_NUMBER,
                _resources,
                currentLocale
            ).toInt()
        } else {
            val currentDate = Date(timeStamp)
            return (SimpleDateFormat("yyyy", currentLocale).format(currentDate)).toInt()
        }
    }

    fun showMonthPickerBottomSheet(
        fragmentManager: FragmentManager,
        _onNewMonthSelected: ((month: Int, year: Int) -> Unit)? = null
    ) {
        val monthPickerInteraction = object : MonthPickerBottomSheet.Interaction {
            override fun onNewMonthSelected(month: Int, year: Int) {
                setMonthAndYear(month, year)
                if (_onNewMonthSelected != null) {
                    _onNewMonthSelected(month, year)
                }
            }

            override fun onNavigateToCurrentMonthClicked() {
                navigateToCurrentMonth()
                if (_onNewMonthSelected != null) {
                    _onNewMonthSelected(
                        getMonth(DateUtils.getCurrentUnixTimeInMilliSeconds()),
                        getYear(DateUtils.getCurrentUnixTimeInMilliSeconds())
                    )
                }
            }
        }
        val monthPicker =
            MonthPickerBottomSheet(
                interaction = monthPickerInteraction,
                isShamsi = sharedPreferences.isCalendarSolar(currentLocale),
                _resources = _resources,
                defaultMonth = getMonth(),
                defaultYear = getYear(),
                isDefaultMonthTheCurrentMonth = getMonth() == getCurrentMonth() && getYear() == getCurrentYear()
            )
        monthPicker.show(fragmentManager, "MonthPicker")

    }

    fun navigateToPreviousMonth() {
        val currentMonth = getMonth()
        val currentYear = getYear()

        var previousMonth = currentMonth.minus(1)
        var finalYear = currentYear
        //check for if user navigate previous year
        if (previousMonth < 1) {
            previousMonth = 12
            finalYear = currentYear.minus(1)
        }
        //set new month & year
        setMonthAndYear(
            month = previousMonth,
            year = finalYear
        )
    }

    fun navigateToNextMonth() {
        val currentMonth = getMonth()
        val currentYear = getYear()

        //check for if user navigate next year
        var nextMonth = currentMonth.plus(1)
        var finalYear = currentYear

        if (nextMonth > 12) {
            nextMonth = 1
            finalYear = currentYear.plus(1)
        }
        //set new month & year

        setMonthAndYear(
            month = nextMonth,
            year = finalYear
        )


    }

    fun navigateToCurrentMonth() {
        setMonthAndYear(
            month = getCurrentMonth(),
            year = getCurrentYear()
        )
    }

    /**
     * this method is being used to refresh month data for when calendar type changed
     * default calendar was gregorian and user change it into solar in setting
     * this method just call setMonthAndYear and reset it with this month value
     * we don't want to set it to old month b/c its interrupt in solar and gregorian month different
     */
    fun refreshData() {

        setMonthAndYear(
            month = getCurrentMonth(),
            year = getCurrentYear()
        )
    }


}