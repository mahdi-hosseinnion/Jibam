package com.ssmmhh.jibam.presentation.util

import android.text.method.ScrollingMovementMethod
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.ConfigurationCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.util.DateUtils
import com.ssmmhh.jibam.util.separate3By3
import com.ssmmhh.jibam.util.toLocaleString
import com.ssmmhh.jibam.util.toLocaleStringWithTwoDigits
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

@BindingAdapter(
    value = ["app:groupNumberByThreeThenSetAsText", "app:prefixText"],
    requireAll = false
)
fun groupNumberByThree(txt: TextView, number: BigDecimal?, prefixText: String? = "") {
    if (number == null) {
        txt.text = ""
        return
    }
    txt.text = prefixText + separate3By3(
        number,
        ConfigurationCompat.getLocales(txt.context.resources.configuration)[0]
    )
}

/**
 * Sets [preferredText] if its not null or empty otherwise sets [alternativeText] as textView's text.
 */
@BindingAdapter(
    value = ["app:preferredText", "app:alternativeText"],
    requireAll = true
)
fun setPreferredTextIfItsNotNullOrEmptyOtherwiseSetAlternativeText(
    view: TextView,
    preferredText: String?,
    alternativeText: String,
) {
    view.text = if (preferredText.isNullOrBlank()) {
        alternativeText
    } else {
        preferredText
    }

}

/**
 *  Convert unix time stamp to human readable date.
 *  Solar hijri patter: month/day/year.
 *  Gregorian pattern: year/month/day.
 */
@BindingAdapter(
    value = ["app:normalDatePattern", "app:isCalendarSolar"],
    requireAll = true
)
fun normalDatePattern(
    view: TextView, date: Long, isCalendarSolar: Boolean,
) {
    val dateHolder = DateUtils.convertUnixTimeToDate(date, isCalendarSolar)
    view.text = if (isCalendarSolar) {
        "${dateHolder.year.toLocaleString()}/${dateHolder.month.toLocaleStringWithTwoDigits()}/${dateHolder.day.toLocaleStringWithTwoDigits()}"
    } else {
        "${dateHolder.month.toLocaleStringWithTwoDigits()}/${dateHolder.day.toLocaleStringWithTwoDigits()}/${dateHolder.year.toLocaleString()}"
    }
}