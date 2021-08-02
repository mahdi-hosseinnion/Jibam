package com.example.jibi.ui.main.transaction.addedittransaction.bottomSheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.view.*
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import biz.laenger.android.vpbs.BottomSheetUtils
import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior
import biz.laenger.android.vpbs.ViewPagerBottomSheetDialogFragment
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.util.PreferenceKeys
import com.example.jibi.util.convertDpToPx
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import uk.co.samuelwall.materialtaptargetprompt.DialogResourceFinder
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.util.*


class CreateNewTransBottomSheet
constructor(
    private val categoryList: List<Category>,
    private val requestManager: RequestManager,
    private val onDismissCallback: OnDismissCallback,
    private val selectedCategoryId: Int,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor,
    private val _resources: Resources
) : ViewPagerBottomSheetDialogFragment(), BottomSheetListAdapter.Interaction,
    ViewPager.OnPageChangeListener {
    private val TAG = "CreateNewTransBottomSheet"

    //widgets
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var rootLayout: CoordinatorLayout? = null
    private var mIndicator: View? = null
    private var backArrow: ImageButton? = null
    private var shadowDividerView: View? = null

    //vars
    private var indicatorWidth = 0
    private var isLeftToRight: Boolean = true
    private var selectedCategory: Category? = null
    private val bottomSheetTopRadios by lazy { convertDpToPx(16) }


    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view = View.inflate(context, R.layout.bottom_sheet_create_new_trans, null)

        tabLayout = view.findViewById(R.id.bottom_sheet_tabs)
        viewPager = view.findViewById(R.id.bottom_sheet_viewpager)
        rootLayout = view.findViewById(R.id.dialog_button_backGround)
        mIndicator = view.findViewById(R.id.indicator)
        backArrow = view.findViewById(R.id.bottom_sheet_back_arrow)
        shadowDividerView = view.findViewById(R.id.shadow_divider_view)
        //for indicator place

//        isLeftToRight = if (appLanguage == Constants.PERSIAN_LANG_CODE)
//            false
//        else
        isLeftToRight =
            (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR)

        Log.d(TAG, "setupDialog: isLeftToRight: $isLeftToRight ")
        Log.d(
            TAG,
            "setupDialog: TextUtilsCompat: ${(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR)} "
        )
        dialog.setContentView(view)
        //creating bottom sheet behavior
        val bottomSheetBehavior = ViewPagerBottomSheetBehavior.from((view.parent) as View)

        bottomSheetBehavior.setBottomSheetCallback(object :
            ViewPagerBottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    hideAppBar()
                } else {
                    showAppBar()
                }
                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    Log.d(
                        "BOTTOMSHEET", "1111 onHidden called"
                    )
                    onDismissCallback.onDismissCalled(selectedCategory)
                    dismiss()
                }
            }


            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                onBottomSheetSlide(slideOffset)
            }

        })

        //hiding app bar at the start
        backArrow?.setOnClickListener {
            this.dismiss()
        }
        //hide app bar when first launch
        hideAppBar()

        setupViewPager()

        showCategoryPromote(dialog)
    }

    private fun showCategoryPromote(dialog: Dialog) {
        if (sharedPreferences.getBoolean(PreferenceKeys.PROMOTE_CATEGORY_LIST, true)) {

            dialog.setOnShowListener { dialog1 ->
                MaterialTapTargetPrompt.Builder(
                    DialogResourceFinder(
                        getDialog()!!
                    ), 0
                )
                    .setPrimaryText(_resources.getString(R.string.category_bottom_sheet_tap_target_primary))
                    .setSecondaryText(_resources.getString(R.string.category_bottom_sheet_tap_target_secondary))
                    .setAnimationInterpolator(FastOutSlowInInterpolator())
                    .setTarget(R.id.parent_bottomSHeet)
                    .setPromptBackground(RectanglePromptBackground())
                    .setPromptFocal(RectanglePromptFocal())
                    .setPromptStateChangeListener { _, state ->
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                            sharedPrefsEditor.putBoolean(
                                PreferenceKeys.PROMOTE_CATEGORY_LIST,
                                false
                            ).apply()
                        }
                    }
                    .show()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        Log.d(
            "BOTTOMSHEET", "1111 ON DISMISS called"
        )
        onDismissCallback.onDismissCalled(selectedCategory)
        super.onDismiss(dialog)
    }

    private fun setupViewPager() {
        viewPager?.offscreenPageLimit = 1
        viewPager?.adapter = SimplePagerAdapter()
        if (!isLeftToRight) {
            viewPager?.currentItem = VIEW_PAGER_SIZE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            tabLayout?.layoutDirection = View.LAYOUT_DIRECTION_LTR
        } else {
            //TODO TEST THIS
            tabLayout?.let {
                ViewCompat.setLayoutDirection(
                    it,
                    ViewCompat.LAYOUT_DIRECTION_LTR
                )
            }
        }


        viewPager?.addOnPageChangeListener(this)
        tabLayout?.setupWithViewPager(viewPager)
        tabLayout?.post {
            //Determine indicator width at runtime
            indicatorWidth =
                tabLayout?.width?.div(tabLayout?.tabCount!!) ?: 0

            //Assign new width
            val indicatorParams = mIndicator!!.layoutParams as FrameLayout.LayoutParams
            indicatorParams.width = indicatorWidth
//            if (appLanguage == Constants.PERSIAN_LANG_CODE) {
//                indicatorParams.leftMargin = indicatorWidth
//            }else if (appLanguage == Constants.ENGLISH_LANG_CODE){
//                indicatorParams.rightMargin = indicatorWidth
//            }
            if (isLeftToRight) {
                //english
                indicatorParams.rightMargin = indicatorWidth
            } else {
                //farsi
                indicatorParams.leftMargin = indicatorWidth
            }
            mIndicator!!.layoutParams = indicatorParams

        }
        BottomSheetUtils.setupViewPager(viewPager)
    }

    private fun hideAppBar() {
        setStyle(
            STYLE_NORMAL,
            R.style.BottomSheetDialogThemeRoundCorner
        )
        backArrow?.visibility = View.INVISIBLE
        shadowDividerView?.visibility = View.GONE

    }

    private fun showAppBar() {
        setStyle(
            STYLE_NORMAL,
            R.style.BottomSheetDefaultDialogTheme
        )
        backArrow?.visibility = View.VISIBLE
        shadowDividerView?.visibility = View.VISIBLE
    }

    override fun onItemSelected(position: Int, item: Category) {
        Log.d(
            "BOTTOMSHEET", "1111 ON ITEM SELECTED"
        )
        selectedCategory = item
        this.dismiss()
//        onCategorySelected.onCategorySelected(item)
    }


    companion object {
        const val VIEW_PAGER_SIZE = 2
    }

    private inner class SimplePagerAdapter
        : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view: View = LayoutInflater.from(context)
                .inflate(R.layout.recycler_view_layout, container, false)
            val recyclerAdapter = initRecycler(view.findViewById(R.id.main_recycler))
            //support rtl
            if (isLeftToRight) {
                if (position == 0) {
                    //expenses type ==1
                    recyclerAdapter.submitList(categoryList.filter { it.type == 1 })
                } else {
                    //income type ==2
                    recyclerAdapter.submitList(categoryList.filter { it.type == 2 })
                }
            } else {
                if (position == 0) {
                    //income type ==2
                    recyclerAdapter.submitList(categoryList.filter { it.type == 2 })
                } else {
                    //expenses type ==1
                    recyclerAdapter.submitList(categoryList.filter { it.type == 1 })
                }
            }
            container.addView(view)
            return view
        }

        private fun initRecycler(
            recycler: RecyclerView
        ): BottomSheetListAdapter {
            var recyclerAdapter: BottomSheetListAdapter
            recycler.apply {
                layoutManager = GridLayoutManager(this@CreateNewTransBottomSheet.context, 4)
                recyclerAdapter = BottomSheetListAdapter(
                    requestManager,
                    this@CreateNewTransBottomSheet,
                    this@CreateNewTransBottomSheet.requireActivity().packageName,
                    selectedCategoryId = selectedCategoryId, _resources = _resources
                )
                adapter = recyclerAdapter
            }
            recycler.isNestedScrollingEnabled = false
            return recyclerAdapter
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun getCount(): Int = VIEW_PAGER_SIZE

        override fun isViewFromObject(view: View, `object`: Any): Boolean = (`object` == view)


        override fun getPageTitle(position: Int): CharSequence {
            val expenses = _resources.getString(R.string.expenses)
            val income = _resources.getString(R.string.income)
//            return if (!isLeftToRight)
            return if (isLeftToRight)
                if (position == 0) expenses else income
            else
                if (position == 0) income else expenses
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        Log.d(TAG, "onPageScrolled: position: $position positionOffset: $positionOffset")
        val params: FrameLayout.LayoutParams =
            mIndicator?.layoutParams as (FrameLayout.LayoutParams)

        //Multiply positionOffset with indicatorWidth to get translation
        var translationOffset: Float = ((positionOffset + position) * indicatorWidth)

//        if (isLeftToRight) {
//            if (appLanguage == Constants.ENGLISH_LANG_CODE) {
////                translationOffset = (indicatorWidth - translationOffset)
//                params.rightMargin = translationOffset.toInt()
//            } else {
//                params.leftMargin = translationOffset.toInt()
//            }
//        } else {
        Log.d(TAG, "onPageScrolled: translationOffset: ${translationOffset.toInt()}")
//            if (appLanguage == Constants.PERSIAN_LANG_CODE) {
//                translationOffset = (indicatorWidth - translationOffset)
//                params.rightMargin = translationOffset.toInt()
////                params.leftMargin = translationOffset.toInt()
//            } else {
//                translationOffset = (indicatorWidth - translationOffset)
//                params.rightMargin = translationOffset.toInt()
//            }
//
//        }
//        if (isLeftToRight) {
//            //english
////            translationOffset = (indicatorWidth - translationOffset)
//            params.leftMargin = translationOffset.toInt()
//        } else {
//            //farsi
        params.leftMargin = translationOffset.toInt()

//        }

        mIndicator?.layoutParams = params
    }

    override fun onPageSelected(position: Int) {}

    override fun onPageScrollStateChanged(state: Int) {}

    interface OnDismissCallback {
        fun onDismissCalled(selectedCategory: Category?)

    }

    private fun onBottomSheetSlide(slideOffset: Float) {
        //change alpha of backArrow
        backArrow?.alpha = slideOffset
        //change background radius
        val bottomSheetBackGround = rootLayout?.background as GradientDrawable ?: return
        val topHeight = (bottomSheetTopRadios * (1f - slideOffset))
        bottomSheetBackGround.cornerRadius = topHeight

    }

}