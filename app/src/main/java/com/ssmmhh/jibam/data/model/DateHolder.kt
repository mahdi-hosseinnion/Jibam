package com.ssmmhh.jibam.data.model

import android.content.res.Resources
import com.ssmmhh.jibam.R

/**
 * An interface to represent date model classes for different calendars.
 */
interface DateHolder {
    /**
     * Year number.
     */
    val year: Int

    /**
     * Month number starting from 1 to 12.
     */
    val month: Int

    /**
     * Day of week number starting from 0 to 6.
     * 0 is SUNDAY and 6 is SATURDAY
     */
    val dayOfMonthNumber: Int


    @Throws(OutOfRangeMonthNumberException::class)
    fun getMonthName(
        resources: Resources
    ): String

    @Throws(OutOfRangeMonthNumberException::class)
    fun getAbbreviationFormOfMonthName(
        resources: Resources
    ): String


}

/**
 * An interface extended from [DateHolder] that contains [dayOfWeekNumber] too.
 * The first day of week is SUNDAY which is 0.
 */
interface DateHolderWithWeekDay : DateHolder {

    val dayOfWeekNumber: Int

    @Throws(OutOfRangeDayOfWeekNumberException::class)
    fun getDayOfWeekName(
        resources: Resources
    ): String = resources.getString(getDayOfWeekNameResId())

    @Throws(OutOfRangeDayOfWeekNumberException::class)
    fun getDayOfWeekNameResId(): Int = when (dayOfWeekNumber) {
        0 -> R.string.sunday
        1 -> R.string.monday
        2 -> R.string.tuesday
        3 -> R.string.wednesday
        4 -> R.string.thursday
        5 -> R.string.friday
        6 -> R.string.saturday
        else -> throw OutOfRangeDayOfWeekNumberException(
            tag = "OutOfRangeDayOfWeekNumberException",
            number = dayOfWeekNumber
        )
    }
}

/**
 * A date holder class for solar hijri date.
 */
open class SolarHijriDateHolder(
    override val year: Int,
    override val month: Int,
    override val dayOfMonthNumber: Int
) : DateHolder {
    @Throws(OutOfRangeMonthNumberException::class)
    override fun getMonthName(resources: Resources): String =
        resources.getString(getSolarHijriMonthNameResId())

    @Throws(OutOfRangeMonthNumberException::class)
    override fun getAbbreviationFormOfMonthName(resources: Resources): String =
        resources.getString(getSolarHijriMonthNameResId())

    @Throws(OutOfRangeMonthNumberException::class)
    fun getSolarHijriMonthNameResId(): Int = when (month) {
        1 -> R.string.Farvardin
        2 -> R.string.Ordibehesht
        3 -> R.string.Khordad
        4 -> R.string.Tir
        5 -> R.string.Mordad
        6 -> R.string.Shahrivar
        7 -> R.string.Mehr
        8 -> R.string.Aban
        9 -> R.string.Azar
        10 -> R.string.Dey
        11 -> R.string.Bahman
        12 -> R.string.Esfand
        else -> throw OutOfRangeMonthNumberException(
            tag = "getSolarHijriMonthNameResId",
            number = month
        )
    }
}

/**
 * A date holder class for gregorian date.
 */
open class GregorianDateHolder(
    override val year: Int,
    override val month: Int,
    override val dayOfMonthNumber: Int
) : DateHolder {
    @Throws(OutOfRangeMonthNumberException::class)
    override fun getMonthName(resources: Resources): String =
        resources.getString(getGregorianMonthNameResId())

    @Throws(OutOfRangeMonthNumberException::class)
    override fun getAbbreviationFormOfMonthName(resources: Resources): String =
        resources.getString(getAbbreviationFormOfGregorianMonthNameResId())

    @Throws(OutOfRangeMonthNumberException::class)
    fun getGregorianMonthNameResId(): Int = when (month) {
        1 -> R.string.January
        2 -> R.string.February
        3 -> R.string.March
        4 -> R.string.April
        5 -> R.string.May
        6 -> R.string.June
        7 -> R.string.July
        8 -> R.string.August
        9 -> R.string.September
        10 -> R.string.October
        11 -> R.string.November
        12 -> R.string.December
        else -> throw OutOfRangeMonthNumberException(
            tag = "getGregorianMonthNameResId",
            number = month
        )
    }

    /**
     * Returns the abbreviation form of the gregorian months name.
     * ex: Jan for January
     * @throws OutOfRangeMonthNumberException
     */
    @Throws(OutOfRangeMonthNumberException::class)
    fun getAbbreviationFormOfGregorianMonthNameResId(): Int = when (month) {
        1 -> R.string.jan
        2 -> R.string.feb
        3 -> R.string.mar
        4 -> R.string.apr
        5 -> R.string.may
        6 -> R.string.jun
        7 -> R.string.jul
        8 -> R.string.aug
        9 -> R.string.sep
        10 -> R.string.oct
        11 -> R.string.nov
        12 -> R.string.dec
        else -> throw OutOfRangeMonthNumberException(
            tag = "getAbbreviationOfGregorianMonthNameResId",
            number = month
        )
    }
}

/**
 * A date holder class for solar hijri date with [dayOfWeekNumber].
 */
data class SolarHijriDateHolderWithWeekDay(
    override val year: Int,
    override val month: Int,
    override val dayOfMonthNumber: Int,
    override val dayOfWeekNumber: Int
) : SolarHijriDateHolder(
    year = year,
    month = month,
    dayOfMonthNumber = dayOfMonthNumber
), DateHolderWithWeekDay

/**
 * A date holder class for gregorian date with [dayOfWeekNumber].
 */
data class GregorianDateHolderWithWeekDay(
    override val year: Int,
    override val month: Int,
    override val dayOfMonthNumber: Int,
    override val dayOfWeekNumber: Int
) : GregorianDateHolder(
    year = year,
    month = month,
    dayOfMonthNumber = dayOfMonthNumber
), DateHolderWithWeekDay

/**
 * An exception that throws when month number value is out of valid range.
 * valid range: 1 to 12.
 */
class OutOfRangeMonthNumberException(val tag: String, val number: Int) :
    Exception(message = "$tag: Month number: $number is out of valid month number range. valid range: 1 to 12")

/**
 * An exception that throws when day of week number value is out of valid range.
 * valid range: 0 to 6.
 */
class OutOfRangeDayOfWeekNumberException(val tag: String, val number: Int) :
    Exception(message = "$tag: DayOfWeek number: $number is out of valid day of week number range. valid range: 0 to 6")
