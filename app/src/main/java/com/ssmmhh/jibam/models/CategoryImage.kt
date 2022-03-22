package com.ssmmhh.jibam.models

import android.content.Context
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage

data class CategoryImage(
    val imageResourceName: String,
    val imageBackgroundColor: String
) {
    fun getImageResourceId(
        context: Context,
    ): Int = getCategoryImageResourceIdFromDrawableByCategoryImage(
        context,
        this.imageResourceName
    )
}