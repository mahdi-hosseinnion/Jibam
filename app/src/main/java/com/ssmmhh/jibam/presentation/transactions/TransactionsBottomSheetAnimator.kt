package com.ssmmhh.jibam.presentation.transactions

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.util.convertDpToPx

/**
 * Adds pull-up/pull-down animation to bottom sheet while dragging up and down.
 * Animate bottom sheet radius change.
 * Animate back button appear/disappear.
 * Animate appbar size and radius change.
 * Animate search button size.
 * Animate handler/handler pin height and alpha.
 */
class TransactionsBottomSheetAnimator(
    private val context: Context,
    private val bottomSheet: View,
    private val bottomSheetAppBar: AppBarLayout,
    private val backButton: View,
    private val searchButton: View,
    private val bottomSheetTopHandler: View,
    private val bottomSheetTopHandlerPin: View,
) : BottomSheetBehavior.BottomSheetCallback() {

    private val TAG = "TransactionsBottomSheet"

    private val backButtonWidth by lazy { context.convertDpToPx(56) }
    private val bottomSheetCornerRadius by lazy { context.convertDpToPx(16) }
    private val collapsedAppBarHeight by lazy { context.convertDpToPx(40) }

    private var lastSlideValue: Float? = null

    private val bottomSheetBackground: GradientDrawable by lazy {
        ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.bottom_sheet_bg,
            null
        ) as GradientDrawable
    }
    private val bottomSheetAppBarBackground: GradientDrawable by lazy {
        ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.bottom_sheet_bg,
            null
        ) as GradientDrawable
    }

    init {
        bottomSheet.background = bottomSheetBackground
        bottomSheetAppBar.background = bottomSheetAppBarBackground
    }

    fun setAnimationStateToExpandedMode() {
        setBottomSheetAppbarLiftableStateTo(false)
        onBottomSheetSlide(1f)
    }

    fun setAnimationStateToCollapsedMode() {
        onBottomSheetSlide(0f)
    }

    private fun onBottomSheetSlide(slideOffset: Float) {
        if (lastSlideValue == slideOffset) return

        backButton.alpha = slideOffset

        val topHeight = if (slideOffset <= 1f)
            (bottomSheetCornerRadius * (1f - slideOffset))
        else {
            0f
        }

        //change bottom sheet radius
        bottomSheetBackground.cornerRadius = topHeight
        bottomSheetAppBarBackground.cornerRadius = topHeight

        //change app bar height
        val appbarViewParams = bottomSheetAppBar.layoutParams
        appbarViewParams.height =
            (collapsedAppBarHeight + (bottomSheetCornerRadius - topHeight.toInt()))
        bottomSheetAppBar.layoutParams = appbarViewParams

        //change buttons height
        val buttonsViewParams = searchButton.layoutParams
        buttonsViewParams.width =
            (collapsedAppBarHeight + (bottomSheetCornerRadius - topHeight.toInt()))
        searchButton.layoutParams = buttonsViewParams

        //change top of bottomSheet height
        val viewParams = bottomSheetTopHandler.layoutParams
        viewParams.height = topHeight.toInt()
        bottomSheetTopHandler.layoutParams = viewParams
        bottomSheetTopHandlerPin.alpha = (1f - slideOffset)

        // make the toolbar close button animation
        val closeButtonParams = backButton.layoutParams as ViewGroup.LayoutParams
        closeButtonParams.width = (slideOffset * backButtonWidth).toInt()
        backButton.layoutParams = closeButtonParams
        lastSlideValue = slideOffset
    }

    private fun setBottomSheetAppbarLiftableStateTo(state: Boolean) {
        bottomSheetAppBar.isLiftOnScroll = false
        bottomSheetAppBar.setLiftable(false)
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {}

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        onBottomSheetSlide(slideOffset)
    }

}