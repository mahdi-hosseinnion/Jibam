package com.ssmmhh.jibam.presentation.util

import android.view.View

/**
 * Two listeners for [layout_toolbar_with_back_btn_with_databinding.xml] layout.
 */
interface ToolbarLayoutListener {

    /**
     * Handle the topAppBar_normal's setNavigationOnClickListener.
     */
    fun onClickOnNavigation(view: View)

    /**
     * Handle the topAppBar_img_btn's setOnClickListener.
     */
    fun onClickOnMenuButton(view: View)
}

/**
 * Two listeners for [toolbar_month_changer_with_databinding.xml] layout.
 */
interface MonthChangerToolbarLayoutListener {

    fun onClickOnMonthName(view: View)

    fun onClickOnPreviousMonthButton(view: View)

    fun onClickOnNextMonthButton(view: View)
}