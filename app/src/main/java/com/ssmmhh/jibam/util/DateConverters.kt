package com.ssmmhh.jibam.util

import android.content.Context
import android.content.res.Resources
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**  Gregorian & Jalali (Hijri_Shamsi,Solar) Date Converter Functions
Author: JDF.SCR.IR =>> Download Full Version :  http://jdf.scr.ir/jdf
License: GNU/LGPL _ Open Source & Free :: Version: 2.80 : [2020=1399]
---------------------------------------------------------------------
355746=361590-5844 & 361590=(30*33*365)+(30*8) & 5844=(16*365)+(16/4)
355666=355746-79-1 & 355668=355746-79+1 &  1595=605+990 &  605=621-16
990=30*33 & 12053=(365*33)+(32/4) & 36524=(365*100)+(100/4)-(100/100)
1461=(365*4)+(4/4) & 146097=(365*400)+(400/4)-(400/100)+(400/400)
source: https://jdf.scr.ir/jdf/kotlin*/
fun convertSolarHijriToGregorian(jy: Int, jm: Int, jd: Int): GregorianDateHolder {
    var jy1: Int = jy + 1595
    var days: Int =
        -355668 + (365 * jy1) + ((jy1 / 33) * 8) + (((jy1 % 33) + 3) / 4) + jd + (if (jm < 7) ((jm - 1) * 31) else (((jm - 7) * 30) + 186))
    var gy: Int = 400 * (days / 146097)
    days %= 146097
    if (days > 36524) {
        gy += 100 * (--days / 36524)
        days %= 36524
        if (days >= 365) days++
    }
    gy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        gy += ((days - 1) / 365)
        days = (days - 1) % 365
    }
    var gd: Int = days + 1
    var sal_a: IntArray = intArrayOf(
        0,
        31,
        if ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 29 else 28,
        31,
        30,
        31,
        30,
        31,
        31,
        30,
        31,
        30,
        31
    )
    var gm: Int = 0
    while (gm < 13 && gd > sal_a[gm]) gd -= sal_a[gm++]

    return GregorianDateHolder(
        year = gy,
        month = gm,
        dayOfMonthNumber = gd
    )
}

/**
 * outPut is unix time in milliseconds at 00:00:00
 */
fun convertSolarHijriToUnixTime(jy: Int, jm: Int, jd: Int): Long {
    val gregorianDate = convertSolarHijriToGregorian(jy, jm, jd)
    return convertGregorianToUnixTime(
        year = gregorianDate.year,
        month = gregorianDate.month,
        day = gregorianDate.dayOfMonthNumber
    )
}

/**
input date should be dd/MM/yyyy
 */
fun convertGregorianToUnixTime(year: Int, month: Int, day: Int): Long {
    val strDate =
        "${day.toStringWith2Digit()}/${month.toStringWith2Digit()}/${year.toStringWith2Digit()}"
    val formatter: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    val date = formatter.parse(strDate) as Date
    return date.time
}

fun convertUnixTimeToSolarHijri(unixTimeStamp: Long): SolarHijriDateHolderWithWeekDay =
    gregorianToShamsiDate(Date(unixTimeStamp))

/**
 * This function is accurate in dates between 2029-03-19 OR 1407/12/29 and 1370/1/1. Reason: after
 * every 33 years solar calendar will be kabise every 5 years instead of 4.
 */
fun gregorianToShamsiDate(gregorianDate: Date): SolarHijriDateHolderWithWeekDay {
    val year: Int
    var date: Int
    val month: Int
    val ld: Int
    val miladiYear: Int = gregorianDate.year + 1900
    val miladiMonth: Int = gregorianDate.month + 1
    val miladiDate: Int = gregorianDate.date
    val WeekDay: Int = gregorianDate.day
    val buf1 = IntArray(12)
    val buf2 = IntArray(12)
    buf1[0] = 0
    buf1[1] = 31
    buf1[2] = 59
    buf1[3] = 90
    buf1[4] = 120
    buf1[5] = 151
    buf1[6] = 181
    buf1[7] = 212
    buf1[8] = 243
    buf1[9] = 273
    buf1[10] = 304
    buf1[11] = 334
    buf2[0] = 0
    buf2[1] = 31
    buf2[2] = 60
    buf2[3] = 91
    buf2[4] = 121
    buf2[5] = 152
    buf2[6] = 182
    buf2[7] = 213
    buf2[8] = 244
    buf2[9] = 274
    buf2[10] = 305
    buf2[11] = 335
    if (miladiYear % 4 != 0) {
        date = buf1[miladiMonth - 1] + miladiDate
        if (date > 79) {
            date = date - 79
            if (date <= 186) {
                when (date % 31) {
                    0 -> {
                        month = date / 31
                        date = 31
                    }
                    else -> {
                        month = date / 31 + 1
                        date = date % 31
                    }
                }
                year = miladiYear - 621
            } else {
                date = date - 186
                when (date % 30) {
                    0 -> {
                        month = date / 30 + 6
                        date = 30
                    }
                    else -> {
                        month = date / 30 + 7
                        date = date % 30
                    }
                }
                year = miladiYear - 621
            }
        } else {
            ld = if (miladiYear > 1996 && miladiYear % 4 == 1) {
                11
            } else {
                10
            }
            date = date + ld
            when (date % 30) {
                0 -> {
                    month = date / 30 + 9
                    date = 30
                }
                else -> {
                    month = date / 30 + 10
                    date = date % 30
                }
            }
            year = miladiYear - 622
        }
    } else {
        date = buf2[miladiMonth - 1] + miladiDate
        ld = if (miladiYear >= 1996) {
            79
        } else {
            80
        }
        if (date > ld) {
            date -= ld
            if (date <= 186) {
                when (date % 31) {
                    0 -> {
                        month = date / 31
                        date = 31
                    }
                    else -> {
                        month = date / 31 + 1
                        date = date % 31
                    }
                }
                year = miladiYear - 621
            } else {
                date -= 186
                when (date % 30) {
                    0 -> {
                        month = date / 30 + 6
                        date = 30
                    }
                    else -> {
                        month = date / 30 + 7
                        date = date % 30
                    }
                }
                year = miladiYear - 621
            }
        } else {
            date = date + 10
            when (date % 30) {
                0 -> {
                    month = date / 30 + 9
                    date = 30
                }
                else -> {
                    month = date / 30 + 10
                    date = date % 30
                }
            }
            year = miladiYear - 622
        }
    }
    return SolarHijriDateHolderWithWeekDay(
        //dayOfWeek number the day of the week represented by this date. The returned value
        // (0 = Sunday, 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 = Friday, 6 = Saturday)
        dayOfWeekNumber = WeekDay,
        year = year,
        month = month,//Month number range (1 to 12). first one is farvardin and last one is esfand.
        dayOfMonthNumber = date,
    )

}

const val maxShamsiYear = 1407
const val maxShamsiMonth = 12
const val maxShamsiDay = 29

const val minShamsiYear = 1370
const val minShamsiMonth = 1
const val minShamsiDay = 1

const val minGregorianDate = 669554735_000
const val maxGregorianYear = 2028
const val minGregorianYear = 1992
const val maxGregorianDate = 1868613935_000
