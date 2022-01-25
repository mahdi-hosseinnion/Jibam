package com.ssmmhh.jibam.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher


fun imageViewWithDrawable(
    @DrawableRes id: Int,
    @ColorRes tint: Int? = null,
    tintMode: PorterDuff.Mode = SRC_IN
) = object : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
        description.appendText("ImageView with drawable same as drawable with id $id")
        tint?.let { description.appendText(", tint color id: $tint, mode: $tintMode") }
    }

    override fun matchesSafely(view: View): Boolean {
        val context = view.context
        val tintColor = tint?.toColor(context)
        val expectedBitmap = context.getDrawable(id)!!.tinted(tintColor, tintMode).toBitmap()

        return view is ImageView && view.drawable.toBitmap().sameAs(expectedBitmap)
    }
}

private fun Drawable.tinted(@ColorInt tintColor: Int? = null, tintMode: PorterDuff.Mode = SRC_IN) =
    apply {
        setTintList(tintColor?.toColorStateList())
        setTintMode(tintMode)
    }

private fun Int.toColorStateList() = ColorStateList.valueOf(this)
private fun Int.toColor(context: Context) = ContextCompat.getColor(context, this)

fun extendedFAB_withIcon(
    @DrawableRes resId: Int
): BoundedMatcher<View, ExtendedFloatingActionButton> = object :
    BoundedMatcher<View, ExtendedFloatingActionButton>(ExtendedFloatingActionButton::class.java) {
    override fun describeTo(description: Description?) {
        description?.appendText("ExtendedFloatingActionButton with drawable same as drawable with resId: $resId ")
    }

    override fun matchesSafely(item: ExtendedFloatingActionButton?): Boolean {
        val expectedBitmap = item!!.context.resources.getDrawable(resId).toBitmap()
        return item.icon.toBitmap().sameAs(expectedBitmap)
    }

}