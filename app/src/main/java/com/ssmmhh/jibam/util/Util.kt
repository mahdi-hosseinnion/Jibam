package com.ssmmhh.jibam.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.presentation.addedittransaction.common.CalculatorKeyboard
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

fun Fragment.convertDpToPx(dp: Int): Int {
    val r = resources
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        r.displayMetrics
    ).toInt()
}


/**
 * remove ',' character that have been used for separate big number 3 by 3
 * like 123,456,789 to 123456789
 */
fun String.removeSeparateSign() = this.replace(",", "")

/**
 * in farsi locale formatter replace '.' with '٫' and ',' with '٬'
 */
const val WRONG_FARSI_LOCALE_DOT = '٫'
const val WRONG_FARSI_LOCALE_NUMBER_SEPARATOR = '٬'
const val DOT = '.'
const val NUMBER_SEPARATOR = ','
fun separate3By3(money1: BigDecimal, locale: Locale): String =
    separate3By3_(money1, locale)
        .replace(WRONG_FARSI_LOCALE_NUMBER_SEPARATOR, NUMBER_SEPARATOR)
        .replace(WRONG_FARSI_LOCALE_DOT, DOT)

private fun separate3By3_(money1: BigDecimal, locale: Locale): String {
    var money = money1
    var isMoneyNegative = false
    if (money < BigDecimal.ZERO) {
        money = money.negate()
        isMoneyNegative = true
    }
    //we use formatter to apply local(farsi digits) and ,
    val formatter: DecimalFormat = NumberFormat.getInstance(locale) as DecimalFormat
    if (money < BigDecimal("1000.0")) {
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

fun localizeDoubleNumber(money1: Double?, locale: Locale): String? {
    if (money1 == null) {
        return null
    }
    var money = money1
    if (money < 0.0) {
        money *= -1.0
    }
    //we use formatter to apply local(farsi digits) and ,
    val formatter: DecimalFormat = NumberFormat.getInstance(locale) as DecimalFormat
    formatter.applyPattern("#.###")
    /**
     * for sum reasons when farsi locale applied to a 'DecimalFormat' it replace '.' with '٫'
     * there's no way to prevent this from happening so i simply use replace to replace '٫' with '.'
     */
    return (formatter.format(money)).replace(WRONG_FARSI_LOCALE_DOT, DOT)
}

fun separate3By3AndRoundIt(money: BigDecimal, locale: Locale): String {
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
fun Float.roundToOneDigit(): Float = Math.round(this * 10.0f) / 10.0f
fun BigDecimal.roundToOneDigit(): BigDecimal = this.setScale(1, BigDecimal.ROUND_HALF_EVEN)


fun calculatePercentage(value: BigDecimal, totalAmount: BigDecimal): BigDecimal =
    ((value.setScale(3).div(totalAmount)).abs()).times(BigDecimal("100"))

fun calculatePercentageAndRoundResult(value: BigDecimal, totalAmount: BigDecimal): BigDecimal =
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

/**
 * Use case: this function is used to receive a text that contains a mix of number and math operation
 * sign like(-, +, ×, ÷) and return the same text but with numbers separated 3 by 3
 * ex: convert 123456+561-123987 to 123,456+561-123,987
 */
fun separateCalculatorText3By3(text: String, locale: Locale): String {
    //remove any , from text
    var value = text.remove3By3Separators()
    //create a simple string builder to hold the result of function
    val result = StringBuilder("")
    //this two variables are used to indicate position of last and current operation sign position
    var lastOperationPosition = 0
    var currentOperationPosition: Int
    //this loop will run untill there is any operation sign(-, +, ×, ÷) left in value
    while (value.indexOfAny(CalculatorKeyboard.listOfOperationsSigns) >= 0) {
        //get first index of any sign in the text
        currentOperationPosition = value.indexOfAny(CalculatorKeyboard.listOfOperationsSigns)
        //extract the number between last and current operation
        var selectedNumber = value.substring(lastOperationPosition, currentOperationPosition)
        //if selectedNumber size is grater then 3 we will separate it
        if (selectedNumber.length > 3) {
            selectedNumber = selectedNumber.convertFarsiDigitsToEnglishDigits().toBigDecimalOrNull()
                ?.let { separate3By3(it, locale) }
                ?: selectedNumber
        }
        //append the number to result
        result.append(selectedNumber)
        //append the sign to result
        result.append(value.substring(currentOperationPosition, currentOperationPosition.plus(1)))
        //remove sign from value (for while loop condition)
        value = value.removeRange(currentOperationPosition, currentOperationPosition.plus(1))

        lastOperationPosition = currentOperationPosition
    }
    //if there is not any sign in the text (ex: 123456) or there is a number after
    //an operation (ex: 134+123456) we want to separate number 3 by 3
    //extract the number from last sign position till end of text
    var lastSectionNumber = value.substring(lastOperationPosition)
    //if selectedNumber size is grater then 3 it will be separate
    if (lastSectionNumber.length > 3) {
        lastSectionNumber =
            lastSectionNumber.convertFarsiDigitsToEnglishDigits().toBigDecimalOrNull()
                ?.let { separate3By3(it, locale) }
                ?: lastSectionNumber
    }
    //append the number to result
    result.append(lastSectionNumber)
    return result.toString()
}

/**
 * this function remove ',' from string
 * it will convert 3 by 3 separated number to a normal number
 * ex: converts 123,456,789 to 123456789
 */
fun String.remove3By3Separators(): String = this.replace(NUMBER_SEPARATOR.toString(), "")

//TODO test what happens if resource does not exitst
fun getResourcesStringValueByName(
    context: Context,
    stringName: String,
): String? = context.run {
    val nameId: Int = resources.getIdentifier(
        stringName,
        "string",
        packageName
    )
    try {
        resources.getString(nameId)
    } catch (e: Exception) {
        Log.e(
            "App",
            "getResourcesStringValueByName: unable to find string with name: '$stringName' in string resources ",
        )
        null
    }
}

//TODO test what happens if resource does not exitst
fun getCategoryImageResourceIdFromDrawableByCategoryImage(
    context: Context,
    categoryImageName: String,
): Int = context.run {
    resources.getIdentifier(
        "ic_cat_$categoryImageName",
        "drawable",
        packageName
    )
}

fun RecyclerView.ViewHolder.getStringFromItemView(id: Int) = itemView.resources.getString(id)