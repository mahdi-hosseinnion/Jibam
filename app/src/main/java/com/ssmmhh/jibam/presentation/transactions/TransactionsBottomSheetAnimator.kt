package com.ssmmhh.jibam.presentation.transactions

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
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
 *
 * TODO("Implement a more smooth way to slide transaction title in app bar.")
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

    private val backButtonMaximumWidth by lazy { context.convertDpToPx(56) }
    private val bottomSheetMaximumCornerRadius by lazy { context.convertDpToPx(16) }
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

    /**
     * [slideOffset] value is floating number which is 0 when bottom sheet is in COLLAPSED and 1 when
     * it is EXPANDED. In DRAGGING state is number between 0 to 1.
     */
    private fun onBottomSheetSlide(slideOffset: Float) {
        if (lastSlideValue == slideOffset) return

        val cornerRadius = if (slideOffset <= 1f)
            (bottomSheetMaximumCornerRadius * (1f - slideOffset))
        else {
            0f
        }

        //change bottom sheet radius
        bottomSheetBackground.cornerRadius = cornerRadius
        bottomSheetAppBarBackground.cornerRadius = cornerRadius


        //change app bar height
        //Expanded bottom sheet app bar height should be [collapsedAppBarHeight] + [bottomSheetCornerRadius]
        val appbarViewParams = bottomSheetAppBar.layoutParams
        appbarViewParams.height =
            (collapsedAppBarHeight + (bottomSheetMaximumCornerRadius - cornerRadius.toInt()))
        //setLayoutParams had to be called for the app bar b/c it is the container of other views.
        bottomSheetAppBar.layoutParams = appbarViewParams

        //change buttons height
        searchButton.layoutParams.apply {
            width =
                (collapsedAppBarHeight + (bottomSheetMaximumCornerRadius - cornerRadius.toInt()))
        }

        //change top of bottomSheet height
        bottomSheetTopHandler.layoutParams.apply {
            height = cornerRadius.toInt()
        }
        bottomSheetTopHandlerPin.alpha = (1f - slideOffset)

        // make the toolbar back button animation
        backButton.layoutParams.apply {
            width = (slideOffset * backButtonMaximumWidth).toInt()
        }
        backButton.alpha = slideOffset

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