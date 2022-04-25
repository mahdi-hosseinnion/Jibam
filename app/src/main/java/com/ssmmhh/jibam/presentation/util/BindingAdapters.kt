package com.ssmmhh.jibam.presentation.util

import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ssmmhh.jibam.R


/**
 * Set textview's movement method to [ScrollingMovementMethod()] for scrollable textview.
 */
@BindingAdapter("app:setMovementMethodToScrolling")
fun setMovementMethodToScrollingTxt(view: TextView, verticallyScrollable: Boolean) {
    if (verticallyScrollable) {
        view.movementMethod = ScrollingMovementMethod()
    }
}

@BindingAdapter("app:loadWithResourceId")
fun loadImageWithResourceIdGlide(view: ImageView, resId: Int) {
    Glide.with(view)
        .load(resId)
        .centerInside()
        .transition(DrawableTransitionOptions.withCrossFade())
        .error(R.drawable.ic_error)
        .into(view)
}