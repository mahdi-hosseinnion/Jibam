package com.ssmmhh.jibam.presentation.util

import android.text.method.ScrollingMovementMethod
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.ConfigurationCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.util.separate3By3
import java.math.BigDecimal


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

@BindingAdapter("app:groupNumberByThreeThenSetAsText")
fun groupNumberByThree(txt: TextView, number: BigDecimal) {
    txt.text = separate3By3(
        number,
        ConfigurationCompat.getLocales(txt.context.resources.configuration)[0]
    )
}