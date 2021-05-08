package com.example.jibi.util

import com.example.jibi.ui.main.transaction.TransactionListAdapter
import java.util.*

//  this algorithm for years and it is very accurate between 1901 and 2099.
//TIP this class only work in date between 2029-03-19/OR/1407/12/29 and 1370/1/1
//b/c 1408 is kabise but 1407 is'nt kabise and 1403 is kabise its all about 33 year kabise in shamsi
//tarikh
//To solve this proble check this two project
//https://github.com/alirezaafkar/SunDatePicker/blob/master/sundatepicker/src/main/java/com/alirezaafkar/sundatepicker/components/JDF.java
//https://github.com/persian-calendar/DroidPersianCalendar
object SolarCalendar {


    private var date: Int = 0
    private var month = 0
    private var year = 0
    private var strMonth: String? = null
    private var strWeekDay: String? = null

    var isThisTest = false

    fun calcSolarCalendar(
        unixTimeStamp: Long,
        pattern: ShamsiPatterns,
        loc: Locale
    ): String =
        calcSolarCalendar(Date(unixTimeStamp), pattern, loc)

//    = Locale("en_US")
    fun calcSolarCalendar(
        gregorianDate: Date,
        pattern: ShamsiPatterns,
        loc: Locale
    ): String {
        val int: Int = 1868613935

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
        when (month) {
            1 -> strMonth = "فروردين"
            2 -> strMonth = "ارديبهشت"
            3 -> strMonth = "خرداد"
            4 -> strMonth = "تير"
            5 -> strMonth = "مرداد"
            6 -> strMonth = "شهريور"
            7 -> strMonth = "مهر"
            8 -> strMonth = "آبان"
            9 -> strMonth = "آذر"
            10 -> strMonth = "دي"
            11 -> strMonth = "بهمن"
            12 -> strMonth = "اسفند"
        }
        when (WeekDay) {
            0 -> strWeekDay = "يکشنبه"
            1 -> strWeekDay = "دوشنبه"
            2 -> strWeekDay = "سه شنبه"
            3 -> strWeekDay = "چهارشنبه"
            4 -> strWeekDay = "پنج شنبه"
            5 -> strWeekDay = "جمعه"
            6 -> strWeekDay = "شنبه"
        }
        //TODO REFACTOR THIS
        return when (pattern) {
            ShamsiPatterns.RECYCLER_VIEW -> {
                "$strWeekDay، ${TransactionListAdapter.DAY_OF_WEEK_MARKER}" +
                        java.lang.String.format(loc, "%d", date) +
                        " " +
                        strMonth +
                        " " +
                        java.lang.String.format(loc, "%02d", year)
            }
            ShamsiPatterns.DETAIL_FRAGMENT -> {
                "" + java.lang.String.format(loc, "%d", year) + "/" + java.lang.String.format(
                    loc,
                    "%d",
                    month
                ) + "/" +
                        java.lang.String.format(loc, "%02d", date) + " (" + strWeekDay + ")"

            }
            ShamsiPatterns.TEST -> {
                "$year/" + java.lang.String.format(
                    loc, "%02d",
                    month
                ) + "/" + java.lang.String.format(
                    loc,
                    "%02d",
                    date
                ) + "/" + strMonth + "/" + strWeekDay
            }
        }

    }

    //* CONST--------------
    //////Shamsi
    //min
    const val minShamsiYear = 1370
    const val minShamsiMonth = 1
    const val minShamsiDay = 1

    //max
    const val maxShamsiYear = 1407
    const val maxShamsiMonth = 12
    const val maxShamsiDay = 29

    //////Gregorian
    //min
    const val minGregorianDate = 669554735_000

    //        const val minGregorianYear = 1991/03/21
//        const val minGregorianMonth = 3
//        const val minGregorianDay = 21
    //max
    const val maxGregorianDate = 1868613935_000

    //        const val maxGregorianYear = 2029/03/19
//        const val maxGregorianMonth = 3
//        const val maxGregorianDay = 19
    //yea integer max value
    enum class ShamsiPatterns {
        RECYCLER_VIEW, DETAIL_FRAGMENT, TEST
    }

}