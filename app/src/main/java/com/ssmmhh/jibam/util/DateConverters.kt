package com.ssmmhh.jibam.util

import com.ssmmhh.jibam.data.model.*
import com.ssmmhh.jibam.util.DateUtils.toMilliSeconds
import com.ssmmhh.jibam.util.DateUtils.toSeconds
import java.util.*


/**
 * Convert Solar hijri to unix time.
 * By first converting the date to gregorian with [convertSolarHijriToGregorianDate] function then
 * uses [convertGregorianDateToUnixTime] function to convert gregorian date to unix time
 *
 * @param jy, solar hijri year.
 * @param jm, solar hijri month.
 * @param jd, solar hijri day.
 */
fun convertSolarHijriDateToUnixTime(
    date: SolarHijriDateHolder,
    timeZone: TimeZone? = null,
    hourOfDay: Int = 0,
    minute: Int = 0,
    second: Int = 0
): Long {
    return convertGregorianDateToUnixTime(
        date = convertSolarHijriToGregorianDate(date),
        hourOfDay = hourOfDay,
        minute = minute,
        second = second,
        timeZone = timeZone,
    )
}

/**
 * Convert gregorian date to unix time using GregorianCalendar.
 * Date should be: year, month(first month value 1, last month 12), day(first day value: 1)
 *
 * @param date, The Gregorian date.
 * @param timeZone, Calendar's timezone. if it is null then uses the device default timeZone.
 * @return The unix time of gregorian date at 00:00:00 in seconds.
 */
fun convertGregorianDateToUnixTime(
    date: GregorianDateHolder,
    timeZone: TimeZone? = null,
    hourOfDay: Int = 0,
    minute: Int = 0,
    second: Int = 0
): Long {
    val calendar = GregorianCalendar().apply {
        //Apply the timeZone if it's not null.
        timeZone?.let { setTimeZone(timeZone) }
        set(
            date.year,
            //GregorianCalendar month starts at 0.
            date.month.minus(1),
            date.day,
            hourOfDay,
            minute,
            second
        )
    }
    return (calendar.timeInMillis).toSeconds()
}

/**
 * Convert unix time stamp to solar hijri date by first converting the unix time to gregorain date
 * then convert gregorian date to solar hijri date.
 *
 * @param unixTimeStamp, The unix timestamp in seconds.
 * @param timeZone, The calendar timeZone, if it is null then uses device default timezone.
 * @return The equivalent solar hijri date.
 */
fun convertUnixTimeToSolarHijriDate(
    unixTimeStamp: Long,
    timeZone: TimeZone? = null
): SolarHijriDateHolderWithWeekDay =
    convertGregorianDateToSolarHijriDate(
        date = convertUnixTimeToGregorianDate(
            unixTimeStamp = unixTimeStamp,
            timeZone = timeZone
        )
    )

/**
 * Convert unix time stamp to solar hijri date by first converting the unix time to gregorain date
 * then convert gregorian date to solar hijri date.
 *
 * @param unixTimeStamp, The unix timestamp in seconds.
 * @param timeZone, The calendar timeZone, if it is null then uses device default timezone.
 * @return The equivalent gregorian date with month starting at 1 and day starting at 1 and dayOfWeek
 *  starting at SUNDAY which is 1 and ending at SATURDAY which is 7.
 */
fun convertUnixTimeToGregorianDate(
    unixTimeStamp: Long,
    timeZone: TimeZone? = null,
): GregorianDateHolderWithWeekDay {
    val calendar = GregorianCalendar().apply {
        //Apply the timeZone if it's not null.
        timeZone?.let { setTimeZone(timeZone) }
        timeInMillis = unixTimeStamp.toMilliSeconds()
    }
    return GregorianDateHolderWithWeekDay(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH).plus(1),
        day = calendar.get(Calendar.DAY_OF_MONTH),
        dayOfWeekNumber = calendar.get(Calendar.DAY_OF_WEEK)
    )
}

/**
 * Convert solar hijri date to corresponding gregorian date.
 * source: https://jdf.scr.ir/jdf/kotlin
 *
 * @param date, The solarHijri date.
 * @return Return an instance of [GregorianDateHolder] which contains year, month and day of month
 * in gregorian.
 */
fun convertSolarHijriToGregorianDate(date: SolarHijriDateHolder): GregorianDateHolder {
    val jy: Int = date.year
    val jm: Int = date.month
    val jd: Int = date.day
    val jy1: Int = jy + 1595
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
    val sal_a: IntArray = intArrayOf(
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
    var gm = 0
    while (gm < 13 && gd > sal_a[gm]) gd -= sal_a[gm++]

    return GregorianDateHolder(
        year = gy,
        month = gm,
        day = gd
    )
}

/**
 * Convert gregorian date to corresponding solar hijri date.
 * source: https://jdf.scr.ir/jdf/kotlin
 *
 * @param date, The gregorian date.
 * @return Return an instance of [SolarHijriDateHolderWithWeekDay] which contains year, month and day of month
 * in gregorian.
 */
fun convertGregorianDateToSolarHijriDate(date: GregorianDateHolderWithWeekDay): SolarHijriDateHolderWithWeekDay {
    val gy: Int = date.year
    val gm: Int = date.month
    val gd: Int = date.day
    var g_d_m: IntArray = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    var gy2: Int = if (gm > 2) (gy + 1) else gy
    var days: Int =
        355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + g_d_m[gm - 1]
    var jy: Int = -1595 + (33 * (days / 12053))
    days %= 12053
    jy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        jy += ((days - 1) / 365)
        days = (days - 1) % 365
    }
    val jm: Int
    val jd: Int
    if (days < 186) {
        jm = 1 + (days / 31)
        jd = 1 + (days % 31)
    } else {
        jm = 7 + ((days - 186) / 30)
        jd = 1 + ((days - 186) % 30)
    }
    return SolarHijriDateHolderWithWeekDay(
        //dayOfWeek number the day of the week represented by this date. The returned value
        // (0 = Sunday, 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 = Friday, 6 = Saturday)
        dayOfWeekNumber = date.dayOfWeekNumber,
        year = jy,
        month = jm,//Month number range (1 to 12). first one is farvardin and last one is esfand.
        day = jd,
    )

}



