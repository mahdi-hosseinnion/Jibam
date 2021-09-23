package com.example.jibi.util

import android.util.Log
import com.example.jibi.R

object CategoriesImageBackgroundColors {

    fun getCategoryColorById(id: Int): Int {
        try {
            if (listOfColor.size > id) {
                return listOfColor[id]
            }
            val divideBy10 = id.div(10)

            if (listOfColor.size > divideBy10) {
                return listOfColor[divideBy10]
            }

            val divideBy100 = id.div(100)
            if (listOfColor.size > divideBy100) {
                return listOfColor[divideBy100]
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCategoryColorById: message: ${e.message}", e)
        }

        return listOfColor[1]
    }

    private val listOfColor: List<Int> = listOf(
        R.color.category_background_color_1,
        R.color.category_background_color_2,
        R.color.category_background_color_3,
        R.color.category_background_color_4,
        R.color.category_background_color_5,
        R.color.category_background_color_6,
        R.color.category_background_color_7,
        R.color.category_background_color_8,
        R.color.category_background_color_9,
        R.color.category_background_color_10,
        R.color.category_background_color_11,
        R.color.category_background_color_12,
        R.color.category_background_color_13,
        R.color.category_background_color_14,
        R.color.category_background_color_15,
        R.color.category_background_color_16,
        R.color.category_background_color_17,
        R.color.category_background_color_18,
        R.color.category_background_color_19,
        R.color.category_background_color_20,
        R.color.category_background_color_21,
        R.color.category_background_color_22,
        R.color.category_background_color_23,
        R.color.category_background_color_24,
        R.color.category_background_color_25,
        R.color.category_background_color_26,
        R.color.category_background_color_27,
        R.color.category_background_color_28,
        R.color.category_background_color_29,
        R.color.category_background_color_30,
        R.color.category_background_color_31,
        R.color.category_background_color_32,
        R.color.category_background_color_33,
        R.color.category_background_color_34,
        R.color.category_background_color_35,
        R.color.category_background_color_36,
        R.color.category_background_color_37,
        R.color.category_background_color_38,
        R.color.category_background_color_39,
        R.color.category_background_color_40,
        R.color.category_background_color_41
    )

}