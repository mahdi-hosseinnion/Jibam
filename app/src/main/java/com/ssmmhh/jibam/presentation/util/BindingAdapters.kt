package com.ssmmhh.jibam.presentation.util

import android.content.res.Resources
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.ConfigurationCompat
import androidx.databinding.BindingAdapter
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.DateHolderWithWeekDay
import com.ssmmhh.jibam.util.*
import com.ssmmhh.jibam.util.DateUtils.toMilliSeconds
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*


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

@BindingAdapter("app:loadWithResourceId")
fun loadImageWithResourceIdGlide(fab: ExtendedFloatingActionButton, resId: Int?) {
    try {
        if (resId != null)
            fab.icon = VectorDrawableCompat.create(fab.resources, resId, null)
    } catch (e: Exception) {
        //Catch:  Resources$NotFoundException: Resource ID #0x0.
        Log.e("ExtendedFab", "loadImageWithResourceIdGlide: ${e.message}", e)
    }
}

@BindingAdapter(
    value = ["app:groupNumberByThreeThenSetAsText", "app:prefixText"],
    requireAll = false
)
fun groupNumberByThree(txt: TextView, number: BigDecimal?, prefixText: String?) {
    if (number == null) {
        txt.text = ""
        return
    }
    txt.text = (prefixText ?: "") + separate3By3(
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

@BindingAdapter("app:disableContentInteraction")
fun disableContentInteraction(edt: EditText, placeHolder: Boolean) {
    edt.keyListener = null
    edt.isFocusable = false
    edt.isFocusableInTouchMode = false
    edt.isCursorVisible = false
    edt.clearFocus()
}

@BindingAdapter(
    value = ["app:completeDateAndTime", "app:isCalendarSolarHijri"],
    requireAll = true
)
fun completeDateAndTime(view: TextView, unixTimeInSeconds: Long, isCalendarSolarHijri: Boolean) {
    val time = SimpleDateFormat("KK:mm aa").format(Date(unixTimeInSeconds.toMilliSeconds()))
    val date = getTheDate(
        unixTimeInSeconds,
        isCalendarSolarHijri,
        view.context.resources
    )
    view.text = "$date $time"
}

private fun getTheDate(
    unixTimeInSeconds: Long,
    isCalendarSolarHijri: Boolean,
    resources: Resources
): String {

    val date: DateHolderWithWeekDay = DateUtils.convertUnixTimeToDate(
        unixTimeInSeconds,
        isCalendarSolarHijri
    )

    //TODO("Retrieve the full form of the day of the week name in English (ex: Monday, not Mon)")
    val dayOfWeekStr = date.getDayOfWeekName(resources)
    val dayStr = date.day.toLocaleStringWithTwoDigits()
    val month = date.getAbbreviationFormOfMonthName(resources)
    val yearStr = date.year.toLocaleString()

    return if (isCalendarSolarHijri) {
        //Solar hijri calendar date format
        "$dayOfWeekStr, $dayStr $month $yearStr"
    } else {
        //Gregorian calendar date format
        //Output example: Sun, 26 Jun 2022.
        "$dayOfWeekStr, $dayStr $month $yearStr"
    }
}