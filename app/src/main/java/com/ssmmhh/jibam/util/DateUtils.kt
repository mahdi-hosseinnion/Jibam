package com.ssmmhh.jibam.util

import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.DateHolder
import com.ssmmhh.jibam.data.model.DateHolderWithWeekDay
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * A bunch of utility function and fields for time
 */
object DateUtils {

    fun getCurrentUnixTimeInMilliSeconds(): Long = System.currentTimeMillis()

    fun getCurrentUnixTimeInSeconds(): Long = getCurrentUnixTimeInMilliSeconds().toSeconds()

    fun getCurrentTime(): Long = getCurrentUnixTimeInSeconds()

    fun convertUnixTimeToDate(
        unixTime: Long,
        isCalendarSolarHijri: Boolean
    ): DateHolderWithWeekDay =
        if (isCalendarSolarHijri) {
            convertUnixTimeToSolarHijriDate(unixTimeStamp = unixTime)
        } else {
            convertUnixTimeToGregorianDate(unixTimeStamp = unixTime)
        }

    val shamsiMonths = arrayOf(
        R.string.Farvardin,
        R.string.Ordibehesht,
        R.string.Khordad,
        R.string.Tir,
        R.string.Mordad,
        R.string.Shahrivar,
        R.string.Mehr,
        R.string.Aban,
        R.string.Azar,
        R.string.Dey,
        R.string.Bahman,
        R.string.Esfand
    )
    val gregorianMonths = arrayOf(
        R.string.January,
        R.string.February,
        R.string.March,
        R.string.April,
        R.string.May,
        R.string.June,
        R.string.July,
        R.string.August,
        R.string.September,
        R.string.October,
        R.string.November,
        R.string.December
    )

    fun Long.toSeconds(): Long = this.div(1_000)
    fun Long.toMilliSeconds(): Long = this.times(1_000)

    /**
     * Minimum and maximum acceptable dates in gregorian and solar hijri.
     * These fields usage is only for presentation purpose ex: MonthPickerBottomSheet's yearNumberPicker and
     * time pickers max and min year.
     */
    const val maxSolarHijriYear = 1478
    const val maxSolarHijriMonth = 12
    const val maxSolarHijriDay = 29
    const val minSolarHijriYear = 1380
    const val minSolarHijriMonth = 1
    const val minSolarHijriDay = 1

    const val maxGregorianYear: Int = 2099
    const val minGregorianYear: Int = 2000
    const val maxGregorianDateInMilliSeconds: Long = 4102444799_000
    const val minGregorianDateInMilliSeconds: Long = 946684830_000
}
