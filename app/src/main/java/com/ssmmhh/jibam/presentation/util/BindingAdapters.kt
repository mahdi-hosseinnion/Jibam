package com.ssmmhh.jibam.presentation.util

import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter


/**
 * Set textview's movement method to [ScrollingMovementMethod()] for scrollable textview.
 */
@BindingAdapter("app:setMovementMethodToScrolling")
fun setMovementMethodToScrollingTxt(view: TextView, verticallyScrollable: Boolean) {
    if (verticallyScrollable) {
        view.movementMethod = ScrollingMovementMethod()
    }
}