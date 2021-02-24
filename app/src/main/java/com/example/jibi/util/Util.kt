package com.example.jibi.util

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