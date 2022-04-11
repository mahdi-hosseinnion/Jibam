package com.ssmmhh.jibam.data.model

import android.content.res.Resources
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.util.localizeNumber

data class Month(
    val startOfMonth: Long,
    val endOfMonth: Long,
    val monthNameResId: Int,
    val year: Int?
)
