package com.ssmmhh.jibam.util

import com.ssmmhh.jibam.R
import java.util.*

//  this algorithm for years and it is very accurate between 1901 and 2099.
//TIP this class only work in date between 2029-03-19/OR/1407/12/29 and 1370/1/1
//b/c 1408 is kabise but 1407 is'nt kabise and 1403 is kabise its all about 33 year kabise in shamsi
//calendar
//To solve this problem check this two project
//TODO USE JDF LIBRARY INSTEAD
//https://jdf.scr.ir/jdf/kotlin
//https://github.com/alirezaafkar/SunDatePicker/blob/master/sundatepicker/src/main/java/com/alirezaafkar/sundatepicker/components/JDF.java
//https://github.com/persian-calendar/DroidPersianCalendar
object ConvertGregorianDateToSolarDate {

    fun convert(unixTimeStamp: Long): SolarDate = convert(Date(unixTimeStamp))

    fun convert(gregorianDate: Date): SolarDate {
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
        val strMonth: Int = when (month) {
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
            else -> R.string.unknown_month
        }
        val strWeekDay: Int = when (WeekDay) {
            0 -> R.string.sunday
            1 -> R.string.monday
            2 -> R.string.tuesday
            3 -> R.string.wednesday
            4 -> R.string.thursday
            5 -> R.string.friday
            6 -> R.string.saturday
            else -> R.string.unknown_day
        }
        return SolarDate(
            strWeekDay = strWeekDay,
            strMonth = strMonth,
            year = year,
            month = month,
            day = date,
        )

    }


    //maximum and minimum date that this class support
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

    data class SolarDate(
        /**
         * Name of day in week represented in string resource id.
         */
        val strWeekDay: Int,
        /**
         * Name of month represented in string resource id.
         */
        val strMonth: Int,
        val day: Int,
        val month: Int,
        val year: Int,
    ) {
        /**
         * Format year base on locale ex) convert 1401 to ۱۴۰۱ if locale is 'fa'
         */
        fun formattedYear(locale: Locale): String = String.format(locale, "%d", this.year)

        /**
         * Format year base on locale ex) convert 1 to ۱ if locale is 'fa'
         */
        fun formattedMonth(locale: Locale): String = String.format(locale, "%d", this.month)

        /**
         * Format year base on locale also add 0 before one digit numbers
         *
         * ex) convert 5 to ۰۵ if locale is 'fa' or 05 if locale is 'en'
         */
        fun formattedDay(locale: Locale): String = String.format(locale, "%02d", this.day)

    }
}