package com.example.jibi.util

import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import com.example.jibi.R
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs

/*fun sortCategoriesWithPinned(categoryList: List<Category>?): List<Category>? {
    if (categoryList == null) {
        return null
    }
    val tempList = ArrayList(categoryList.sortedByDescending { it.ordering })
    val pinedList = categoryList.filter { it.ordering < 0 }.sortedByDescending { it.ordering }
    tempList.removeAll(pinedList)
    tempList.addAll(0, pinedList)
    return tempList
}*/

public fun Fragment.convertDpToPx(dp: Int): Int {
    val r = resources
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        r.displayMetrics
    ).toInt()
}

fun separate3By3(money1: Double, locale: Locale): String =
    separate3By3_(money1, locale).replace('٬', ',')

private fun separate3By3_(money1: Double, locale: Locale): String {
    var money = money1
    var isMoneyNegative = false
    if (money < 0.0) {
        money *= -1.0
        isMoneyNegative = true
    }
    //we use formatter to apply local(farsi digits) and ,
    val formatter: DecimalFormat = NumberFormat.getInstance(locale) as DecimalFormat
    if (money < 1000.0) {
        return if (isMoneyNegative)
            if (locale.isFarsi())
                "${formatter.format(money)}-"
            else
                "-${formatter.format(money)}"
        else
            formatter.format(money)
    }
    formatter.applyPattern("#,###,###,###.###")

    return if (isMoneyNegative)
        if (locale.isFarsi())
            "${formatter.format(money)}-"
        else
            "-${formatter.format(money)}"
    else
        formatter.format(money)
}

fun localizeDoubleNumber(money1: Double, locale: Locale): String {
    var money = money1
    if (money < 0.0) {
        money *= -1.0
    }
    //we use formatter to apply local(farsi digits) and ,
    val formatter: DecimalFormat = NumberFormat.getInstance(locale) as DecimalFormat
    formatter.applyPattern("#.###")
    if (money < 1000.0) {
        return formatter.format(money)
    }
    return formatter.format(money)
}

fun separate3By3AndRoundIt(money: Double, locale: Locale): String {
    //TIP this method will round -354.3999999999942 to -354.4
    val finalResult = separate3By3(money, locale)

    if ((finalResult.indexOf('.')) == -1) {
        return finalResult
    }
    //round part
    if (finalResult.substring(finalResult.lastIndex.minus(1)) == ".0") {
        //convert 15.0 to 15
        return finalResult.substring(
            startIndex = 0,
            endIndex = finalResult.lastIndex.minus(1)
        )
    }
    val periodPosition = finalResult.indexOf('.')

    return if (periodPosition > -1 && periodPosition.plus(3) < finalResult.length) {
        //convert 19.23423424 to 19.23
        finalResult.substring(0, periodPosition.plus(3))
    } else {
        finalResult
    }

}

fun convertDoubleToString(text: String): String {//convert 13.0 to 13
    if (text.length < 2) {
        return text
    }
    return if (text.substring(text.lastIndex.minus(1)) == ".0") //convert 13.0 to 13
        text.substring(0, text.lastIndex.minus(1))
    else
        text
}

//fun convertDoubleToStringAndLocalized(text: String, locale: Locale): String {//convert 13.0 to 13
//    //add farsi language numbers
//    val formatter= Format.getInstance(locale)
//
//    if (text.length < 2) {
//        return formatter.format(text)
//
//    }
//    return if (text.substring(text.lastIndex.minus(1)) == ".0") //convert 13.0 to 13
//        formatter.format(text.substring(0, text.lastIndex.minus(1)))
//    else
//        formatter.format(text)
//}
fun String.convertFarsiDigitsToEnglishDigits() =
    this.replace("۰", "0")
        .replace("۱", "1")
        .replace("۲", "2")
        .replace("۳", "3")
        .replace("۴", "4")
        .replace("۵", "5")
        .replace("۶", "6")
        .replace("۷", "7")
        .replace("۸", "8")
        .replace("۹", "9")

//TODO REPLACE THIS WITH R.string.-0
fun String.convertEnglishDigitsToFarsiDigits() =
    this.replace("0", "۰")
        .replace("1", "۱")
        .replace("2", "۲")
        .replace("3", "۳")
        .replace("4", "۴")
        .replace("5", "۵")
        .replace("6", "۶")
        .replace("7", "۷")
        .replace("8", "۸")
        .replace("9", "۹")

fun String.localizeNumber(resources: Resources) =
    this.replace("0", resources.getString(R.string._0))
        .replace("1", resources.getString(R.string._1))
        .replace("2", resources.getString(R.string._2))
        .replace("3", resources.getString(R.string._3))
        .replace("4", resources.getString(R.string._4))
        .replace("5", resources.getString(R.string._5))
        .replace("6", resources.getString(R.string._6))
        .replace("7", resources.getString(R.string._7))
        .replace("8", resources.getString(R.string._8))
        .replace("9", resources.getString(R.string._9))

fun String.convertLocaleNumberToEnglish(resources: Resources) =
    this.replace(resources.getString(R.string._0), "0")
        .replace(resources.getString(R.string._1), "1")
        .replace(resources.getString(R.string._2), "2")
        .replace(resources.getString(R.string._3), "3")
        .replace(resources.getString(R.string._4), "4")
        .replace(resources.getString(R.string._5), "5")
        .replace(resources.getString(R.string._6), "6")
        .replace(resources.getString(R.string._7), "7")
        .replace(resources.getString(R.string._8), "8")
        .replace(resources.getString(R.string._9), "9")

fun trySafe(method: () -> Unit) =
    try {
        method()
    } catch (e: Exception) {
        Log.d(TAG, "trySafe: e: $e")
    }

fun Double.roundToOneDigit(): Double = Math.round(this * 10.0) / 10.0


fun calculatePercentage(value: Double, totalAmount: Double): Double =
    (abs(value.div(totalAmount))).times(100)

fun calculatePercentageAndRoundResult(value: Double, totalAmount: Double): Double =
    calculatePercentage(value, totalAmount).roundToOneDigit()

/**
 * convert 1 to "01"
 */
fun Int.toStringWith2Digit(): String = if (this < 10)
    "0$this"
else
    this.toString()

fun Locale.isFarsi(): Boolean = (language == Constants.PERSIAN_LANG_CODE)

fun SharedPreferences.isCalendarSolar(currentLocale: Locale): Boolean {
    val calendarType = this.getString(
        PreferenceKeys.APP_CALENDAR_PREFERENCE,
        PreferenceKeys.calendarDefault(currentLocale)
    )
    return calendarType == PreferenceKeys.CALENDAR_SOLAR
}
