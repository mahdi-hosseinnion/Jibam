package com.example.jibi.util

import android.app.Activity
import android.util.TypedValue
import androidx.fragment.app.Fragment
import com.example.jibi.models.Category

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