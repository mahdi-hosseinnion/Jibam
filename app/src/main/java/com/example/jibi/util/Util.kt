package com.example.jibi.util

import android.app.Activity
import android.util.TypedValue
import androidx.fragment.app.Fragment
import com.example.jibi.models.Category
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

fun sortCategoriesWithPinned(categoryList: List<Category>?): List<Category>? {
    if (categoryList == null) {
        return null
    }
    val tempList = ArrayList(categoryList.sortedByDescending { it.ordering })
    val pinedList = categoryList.filter { it.ordering < 0 }.sortedByDescending { it.ordering }
    tempList.removeAll(pinedList)
    tempList.addAll(0, pinedList)
    return tempList
}

public fun Fragment.convertDpToPx(dp: Int): Int {
    val r = resources
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        r.displayMetrics
    ).toInt()
}

fun separate3By3(money1: Double, locale: Locale): String {
    var money = money1
    if (money < 0.0) {
        money *= -1.0
    }
    //we use formatter to apply local(farsi digits) and ,
    val formatter: DecimalFormat = NumberFormat.getInstance(locale) as DecimalFormat
    if (money < 1000.0) {
        return formatter.format(money)
    }
    formatter.applyPattern("#,###,###,###.###")
    return formatter.format(money)
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