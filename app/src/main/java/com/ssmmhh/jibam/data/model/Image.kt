package com.ssmmhh.jibam.data.model

import android.content.Context
import com.ssmmhh.jibam.util.getCategoryImageResourceIdFromDrawableByCategoryImage

data class Image(
    val resourceName: String,
    val backgroundColor: String
) {
    fun getImageResourceId(
        context: Context,
    ): Int = getCategoryImageResourceIdFromDrawableByCategoryImage(
        context,
        this.resourceName
    )
}