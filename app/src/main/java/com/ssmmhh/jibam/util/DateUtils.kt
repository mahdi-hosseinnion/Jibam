package com.ssmmhh.jibam.util

import com.ssmmhh.jibam.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * A bunch of utility function and variable for time
 */
object DateUtils {

    fun getCurrentUnixTimeInMilliSeconds(): Long = System.currentTimeMillis()

    fun getCurrentUnixTimeInSeconds(): Long = ((System.currentTimeMillis()).div(1_000))

    fun getCurrentTime(): Long = getCurrentUnixTimeInSeconds()

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
    fun Long.toSeconds():Long = this.div(1_000)
    fun Long.toMilliSeconds():Long = this.times(1_000)

}
