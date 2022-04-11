package com.ssmmhh.jibam.presentation.common

import android.content.SharedPreferences
import androidx.fragment.app.FragmentManager
import com.ssmmhh.jibam.data.model.GregorianDateHolder
import com.ssmmhh.jibam.data.model.Month
import com.ssmmhh.jibam.data.model.SolarHijriDateHolder
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
    private val sharedPreferences: SharedPreferences
) {
    private val TAG = "MonthManger"

    private val _fromDate = MutableStateFlow(
        getStartOfCurrentMonth(
            DateUtils.getCurrentUnixTimeInSeconds()
        )
    )
    private val _toDate = MutableStateFlow(
        getEndOfCurrentMonth(
            DateUtils.getCurrentUnixTimeInSeconds()
        )
    )

    val currentMonth: Flow<Month> = combine(
        _fromDate,
        _toDate
    ) { from, to ->

        return@combine Month(
            startOfMonth = from,
            endOfMonth = to,
            monthNameResId = getMonthResId(from),
            year = getYearOrNullIfIsTheSameAsCurrentYear(from)
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
        return convertSolarHijriDateToUnixTime(
            SolarHijriDateHolder(
                year = currentYear,
                month = currentMonth,
                day = 1
            )
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
        return convertSolarHijriDateToUnixTime(
            SolarHijriDateHolder(
                year = year,
                month = month,
                day = 1
            )
        )
    }

    fun getStartOfCurrentMonthGeorgian(
        currentMonth: Int, currentYear: Int
    ): Long = convertGregorianDateToUnixTime(
        GregorianDateHolder(year = currentYear, month = currentMonth, 1)
    )


    fun getEndOfCurrentMonthGeorgian(
        currentMonth: Int, currentYear: Int
    ): Long {
        var year = currentYear
        var month = currentMonth.plus(1)

        if (month > 12) {
            month = 1
            year = currentYear.plus(1)
        }
        return convertGregorianDateToUnixTime(
            GregorianDateHolder(
                year = year,
                month = month,
                day = 1
            )
        )
    }

    //utils
    fun getEndOfCurrentMonthGeorgian(
        timeStamp: Long
    ): Long {
        //get the month and year of given timeStamp
        val date = convertUnixTimeToGregorianDate(timeStamp)
        return getEndOfCurrentMonthGeorgian(date.month, date.year)
    }

    fun getStartOfCurrentMonthGeorgian(
        timeStamp: Long
    ): Long {
        //get month and year for given timeStamp
        val date = convertUnixTimeToGregorianDate(timeStamp)
        return getStartOfCurrentMonthGeorgian(date.month, date.year)
    }

    fun getEndOfCurrentMonthShamsi(
        timeStamp: Long
    ): Long {
        //get month and year for given timeStamp
        val shamsiDate = convertUnixTimeToSolarHijriDate(timeStamp)
        return getEndOfCurrentMonthShamsi(shamsiDate.month, shamsiDate.year)
    }

    fun getStartOfCurrentMonthShamsi(
        timeStamp: Long
    ): Long {
        //get month and year for given timeStamp
        val shamsiDate = convertUnixTimeToSolarHijriDate(timeStamp)
        return getStartOfCurrentMonthShamsi(shamsiDate.month, shamsiDate.year)
    }

    private fun getYearOrNullIfIsTheSameAsCurrentYear(timeStamp: Long = _fromDate.value): Int? {
        val year = getYear(timeStamp)
        return if (year == getCurrentYear()) null
        else year
    }

    fun getCurrentMonth(): Int = getMonth(DateUtils.getCurrentUnixTimeInSeconds())

    fun getMonthResId(timeStamp: Long = _fromDate.value): Int {
        return DateUtils.convertUnixTimeToDate(
            unixTime = timeStamp,
            isCalendarSolarHijri = sharedPreferences.isCalendarSolar(currentLocale)
        ).getAbbreviationFormOfMonthNameResId()
    }
    fun getMonth(timeStamp: Long = _fromDate.value): Int {
        return DateUtils.convertUnixTimeToDate(
            unixTime = timeStamp,
            isCalendarSolarHijri = sharedPreferences.isCalendarSolar(currentLocale)
        ).month
    }

    fun getCurrentYear(): Int = getYear(DateUtils.getCurrentUnixTimeInSeconds())

    fun getYear(timeStamp: Long = _fromDate.value): Int {
        return DateUtils.convertUnixTimeToDate(
            unixTime = timeStamp,
            isCalendarSolarHijri = sharedPreferences.isCalendarSolar(currentLocale)
        ).year
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
                        getMonth(DateUtils.getCurrentUnixTimeInSeconds()),
                        getYear(DateUtils.getCurrentUnixTimeInSeconds())
                    )
                }
            }
        }
        val monthPicker =
            MonthPickerBottomSheet(
                interaction = monthPickerInteraction,
                isShamsi = sharedPreferences.isCalendarSolar(currentLocale),
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