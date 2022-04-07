package com.ssmmhh.jibam.data.model

import android.content.res.Resources
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.util.localizeNumber

data class Month(
    val startOfMonth: Long,
    val endOfMonth: Long,
    val monthNumber: Int,
    val isShamsi: Boolean,
    val year: Int?
) {
    fun getMonthName(resources: Resources): String {
        val resId: Int = if (isShamsi) {
            when (monthNumber) {
                1 -> R.string.Farvardin
                2 -> R.string.Ordibehesht
                3 -> R.string.Khordad
                4 -> R.string.Tir
                5 -> R.string.Mordad
                6 -> R.string.Shahrivar
                7 -> R.string.Mehr
                8 -> R.string.Aban
                9 -> R.string.Azar
                10 -> R.string.Dey
                11 -> R.string.Bahman
                12 -> R.string.Esfand
                else -> R.string.unknown_month
            }
        } else {
            when (monthNumber) {
                1 -> R.string.jan
                2 -> R.string.feb
                3 -> R.string.mar
                4 -> R.string.apr
                5 -> R.string.may
                6 -> R.string.jun
                7 -> R.string.jul
                8 -> R.string.aug
                9 -> R.string.sep
                10 -> R.string.oct
                11 -> R.string.nov
                12 -> R.string.dec
                else -> R.string.unknown_month
            }
        }
        val month = resources.getString(resId)
        return year?.let {
            "$month\n${year.toString().localizeNumber(resources)}"
        } ?: month
    }
}
