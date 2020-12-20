package com.example.jibi.ui.main.transaction.bottomSheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import biz.laenger.android.vpbs.BottomSheetUtils
import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior
import biz.laenger.android.vpbs.ViewPagerBottomSheetDialogFragment
import com.example.jibi.R
import com.example.jibi.models.Category
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.bottom_sheet_create_new_trans.*
import kotlinx.android.synthetic.main.fragment_screen_slide_page.*
import java.util.*
import kotlin.collections.ArrayList
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.jibi.ui.main.transaction.TransactionFragmentDirections


class CreateNewTransBottomSheet
constructor(
    private val categoryList: List<Category>
) : ViewPagerBottomSheetDialogFragment(), BottomSheetListAdapter.Interaction,
    ViewPager.OnPageChangeListener {
    private val TAG = "DialogFragment"

    //    var bottomSheetToolbar: Toolbar? = null
    var isSearchModeEnable: Boolean = false
    var bottomSheetTabLayout: TabLayout? = null
    var bottomSheetViewPager: ViewPager? = null
    var dialog_button_backGround: CoordinatorLayout? = null
    var mIndicator: View? = null
    var bottom_sheet_back_arrow: ImageButton? = null
    var shadow_divider_view: View? = null
    var bottom_sheet_tab_view_container: FrameLayout? = null
    private var indicatorWidth = 0
    private var isLeftToRight: Boolean = true
//    var bottomSheetBehavior: ViewPagerBottomSheetBehavior<ConstraintLayout>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view = View.inflate(context, R.layout.bottom_sheet_create_new_trans, null)

//        bottomSheetToolbar = view.findViewById(R.id.bottom_sheet_toolbar)
        bottomSheetTabLayout = view.findViewById(R.id.bottom_sheet_tabs)
        bottomSheetViewPager = view.findViewById(R.id.bottom_sheet_viewpager)
        dialog_button_backGround = view.findViewById(R.id.dialog_button_backGround)
        mIndicator = view.findViewById(R.id.indicator)
        bottom_sheet_back_arrow = view.findViewById(R.id.bottom_sheet_back_arrow)
        bottom_sheet_tab_view_container = view.findViewById(R.id.bottom_sheet_tab_view_container)
        shadow_divider_view = view.findViewById(R.id.shadow_divider_view)
//        bottom_sheet_search_button = view.findViewById(R.id.bottom_sheet_search)
//        bottom_sheet_search_button = view.findViewById(R.id.bottom_sheet_search)
        //for indicator place
        isLeftToRight =
            (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR)

        //TODO WITHOUT APP BAR
/*        appBarLayout = view.findViewById(R.id.bottom_sheet_appBar)
        view_small_line1 = view.findViewById(R.id.view_small_line)
        cancelBtn = view.findViewById(R.id.cancelBtn)*/


        dialog.setContentView(view)
//        val bottomSheetBehavior1 =
        val bottomSheetBehavior = ViewPagerBottomSheetBehavior.from((view.parent) as View)

        bottomSheetBehavior.setBottomSheetCallback(object :
            ViewPagerBottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (BottomSheetBehavior.STATE_EXPANDED == newState) {

                    showAppBar()

                } else {
//                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    hideAppBar();
//                    showAppBar();
//                }
                }
                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    dismiss();
                }
            }


            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        })
        //hiding app bar at the start
        bottom_sheet_back_arrow?.setOnClickListener {
            this.dismiss()
        }

        hideAppBar()

        setupViewPager()
    }

    override fun onItemSelected(position: Int, item: Category) {
        val action =
            TransactionFragmentDirections.actionTransactionFragmentToCreateTransactionFragment(
                categoryId = item.id
            )
        this.dismiss()
        findNavController().navigate(action)
    }

    override fun restoreListPosition() {

    }

    private fun setupViewPager() {
        bottomSheetViewPager?.offscreenPageLimit = 1
        bottomSheetViewPager?.adapter = SimplePagerAdapter()
        if (!isLeftToRight) {
            bottomSheetViewPager?.currentItem = 2

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            bottomSheetTabLayout?.layoutDirection = View.LAYOUT_DIRECTION_LTR
        } else {
            //TODO TEST THIS
            ViewCompat.setLayoutDirection(bottomSheetTabLayout!!, ViewCompat.LAYOUT_DIRECTION_LTR)
        }


        bottomSheetViewPager?.addOnPageChangeListener(this)
        bottomSheetTabLayout?.setupWithViewPager(bottomSheetViewPager)
        bottomSheetTabLayout?.post {
            //Determine indicator width at runtime
            indicatorWidth =
                bottomSheetTabLayout?.getWidth()?.div(bottomSheetTabLayout?.tabCount!!) ?: 0

            //Assign new width
            val indicatorParams = mIndicator!!.layoutParams as FrameLayout.LayoutParams
            indicatorParams.width = indicatorWidth
            mIndicator!!.layoutParams = indicatorParams

        }
        BottomSheetUtils.setupViewPager(bottomSheetViewPager)
    }
    //TODO I DONT THINK THIS WILL NIDDED
//    override fun onStart() {
//        super.onStart()
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//    }

    private fun hideAppBar() {
        setStyle(
            ViewPagerBottomSheetDialogFragment.STYLE_NORMAL,
            R.style.BottomSheetDialogThemeRoundCorner
        )
        dialog_button_backGround?.setBackgroundResource(R.drawable.bottom_sheet_bg)
        bottom_sheet_back_arrow?.visibility = View.INVISIBLE
        shadow_divider_view?.visibility = View.GONE

        //TODO WITHOUT APP BAR
//        appBarLayout!!.visibility = View.GONE
//        view_small_line1!!.visibility = View.VISIBLE

    }

    private fun showAppBar() {
        setStyle(
            ViewPagerBottomSheetDialogFragment.STYLE_NORMAL,
            R.style.BottomSheetDefaultDialogTheme
        )
        dialog_button_backGround?.setBackgroundResource(R.color.white)
        bottom_sheet_back_arrow?.visibility = View.VISIBLE
        shadow_divider_view?.visibility = View.VISIBLE
        //TODO WITHOUT APP BAR
/*        appBarLayout!!.visibility = View.VISIBLE
        view_small_line1!!.visibility = View.GONE*/
    }

    /*    private fun hideAppBar(view: View) {
            val params = view.layoutParams
            params.height = 0
            view.layoutParams = params
        }

        private fun showView(view: View, size: Int) {
            val params = view.layoutParams
            params.height = size
            view.layoutParams = params
        }

        private fun getActionBarSize(): Int {
            val array = context!!.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
            return array.getDimension(0, 0f).toInt()
        }*/


    fun extraSetting() {
/*
       //setting Peek at the 16:9 ratio keyline of its parent.
        bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);

        //setting max height of bottom sheet
        bi.extraSpace.setMinimumHeight((Resources.getSystem().getDisplayMetrics().heightPixels) / 2);
        */
    }

    companion object {
        const val VIEW_PAGER_SIZE = 2
    }

    private inner class SimplePagerAdapter
        : PagerAdapter() {

        fun initRecycler(
            recycler: RecyclerView
        ): BottomSheetListAdapter {
            var recyclerAdapter: BottomSheetListAdapter
            recycler.apply {
                layoutManager = GridLayoutManager(this@CreateNewTransBottomSheet.context, 4)
                recyclerAdapter = BottomSheetListAdapter(
                    null,
                    this@CreateNewTransBottomSheet
                )
                adapter = recyclerAdapter
            }
//            recyclerAdapter.submitList(categoryList)
            recycler.isNestedScrollingEnabled = false
            return recyclerAdapter
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var view: View = LayoutInflater.from(context)
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

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }


        override fun getCount(): Int = VIEW_PAGER_SIZE

        override fun isViewFromObject(view: View, `object`: Any): Boolean = `object` == view


        override fun getPageTitle(position: Int): CharSequence? =
            if (isLeftToRight)
                if (position == 0) "Expenses" else "Income"
            else
                if (position == 0) "Income" else "Expenses"


        fun createFakeList(msg: String): List<String> {
            val results: ArrayList<String> = ArrayList<String>()
            for (i in 0..20) {
                results.add("$msg __ " + createRandomText())
            }
            return results
        }

        fun createRandomText(): String = UUID.randomUUID().toString()

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        Log.d(
            TAG,
            "onPageScrolled: position: $position positionOffset: $positionOffset positionOffsetPixels: $positionOffsetPixels"
        )
        val params: FrameLayout.LayoutParams =
            mIndicator?.getLayoutParams() as (FrameLayout.LayoutParams)
        //Multiply positionOffset with indicatorWidth to get translation
        var translationOffset: Float
        if (isLeftToRight) {
            translationOffset = ((positionOffset + position) * indicatorWidth)
            Log.d(TAG, "onPageScrolled: translationOffset: $translationOffset")
            params.leftMargin = translationOffset.toInt()

        } else {
            translationOffset = ((positionOffset + position) * indicatorWidth)
            Log.d(TAG, "onPageScrolled: translationOffset: $translationOffset")
            translationOffset = (indicatorWidth - translationOffset)
            params.rightMargin = translationOffset.toInt()

        }

        mIndicator?.layoutParams = params
    }

    //works fine
/*    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        Log.d(
            TAG,
            "onPageScrolled: position: $position positionOffset: $positionOffset positionOffsetPixels: $positionOffsetPixels"
        )
        val params: FrameLayout.LayoutParams =
            mIndicator?.getLayoutParams() as (FrameLayout.LayoutParams)
        //Multiply positionOffset with indicatorWidth to get translation
        var translationOffset: Float
        if (isLeftToRight) {
            translationOffset = ((positionOffset + position) * indicatorWidth)


            params.leftMargin = translationOffset.toInt()

        } else {
            if (position == 1) {
                translationOffset = (((positionOffset) + position) * indicatorWidth)
            } else if (position == 0) {
                translationOffset = (((1 - positionOffset) + position) * indicatorWidth)

            } else {
                translationOffset = ((positionOffset + position) * indicatorWidth)
            }

            if (position != 1) {
                params.rightMargin = translationOffset.toInt()
            } else {
                params.leftMargin = translationOffset.toInt()

            }
            /*            val newPositions = (VIEW_PAGER_SIZE - (position + 1))
            translationOffset = ((positionOffset + position) * indicatorWidth)
            Log.d(TAG, "onPageScrolled: postions now is = $newPositions")
            Log.d(TAG, "onPageScrolled: translationOffset: $translationOffset")
            translationOffset = (indicatorWidth - translationOffset)
            params.rightMargin = translationOffset.toInt()*/
        }

        mIndicator?.layoutParams = params
    }*/
    override fun onPageSelected(position: Int) {}

    override fun onPageScrollStateChanged(state: Int) {}

}

/*: BottomSheetDialogFragment(),
//) : ViewPagerBottomSheetDialogFragment(),
BottomSheetListAdapter.Interaction {
private val TAG = "CreateNewTransBottomShe"
private lateinit var recyclerAdapter: BottomSheetListAdapter

private lateinit var pagerAdapter: ScreenSlidePagerAdapter

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // The pager adapter, which provides the pages to the view pager widget.
    initRecyclerView()
//        val pagerAdapter = Adapter(activity!!.supportFragmentManager)
//

//        bottom_sheet_viewPager.adapter = pagerAdapter

//        bottom_sheet_viewPager.registerOnPageChangeCallback(object :
//            ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
////                if (position == 0) {
////                    enableExpensesMode()
////                } else {
////                    enableIncomeMode()
////                }
//            }
//        })
}

class DialogFragment
    (private val categoryList: List<Category>) : ViewPagerBottomSheetDialogFragment() {
    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(
            this.requireContext(),
            R.layout.bottom_sheet_create_new_trans,
            null
        )

        val viewPager = contentView.findViewById(R.id.bottom_sheet_viewPager) as ViewPager

        viewPager.adapter = ScreenSlidePagerAdapter(childFragmentManager)

        BottomSheetUtils.setupViewPager(viewPager)
        dialog.setContentView(contentView)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }
    private inner class ScreenSlidePagerAdapter(context:Context) :
        PagerAdapter() {
        override fun getPageTitle(position: Int): CharSequence? {
            return context!!.getString(R.string.tab) + " " + (position + 1).toString()
        }

        override fun getCount(): Int {
            return 2
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return `object` === view
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view: View = LayoutInflater.from(container.context)
                .inflate(R.layout.fragment_nested_scroll, container, false)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }
}
/*//        txt_switch_expenses.setOnClickListener {
//            enableExpensesMode()
//        }
//        txt_switch_income.setOnClickListener {
//            enableIncomeMode()
//        }

}

private fun enableExpensesMode() {
    bottom_sheet_viewPager.setCurrentItem(0, true)
    txt_switch_expenses.setTextColor(
        getColor(
            this.requireContext(),
            R.color.material_on_background_emphasis_high_type
        )
    )
    txt_switch_income.setTextColor(
        getColor(
            this.requireContext(),
            R.color.material_on_background_disabled
        )
    )
}


private fun enableIncomeMode() {
    bottom_sheet_viewPager.setCurrentItem(1, true)
    txt_switch_expenses.setTextColor(
        getColor(
            this.requireContext(),
            R.color.material_on_background_disabled
        )
    )
    txt_switch_income.setTextColor(
        getColor(
            this.requireContext(),
            R.color.material_on_background_emphasis_high_type
        )
    )
}*/

private fun initRecyclerView() {
//        fakeRecycler.apply {
//            layoutManager = GridLayoutManager(this@CreateNewTransBottomSheet.context, 4)
//            recyclerAdapter = BottomSheetListAdapter(
//                null,
//                null
//            )
//            adapter = recyclerAdapter
//        }
//        recyclerAdapter.submitList(categoryList)
//        fakeRecycler.isNestedScrollingEnabled = false
}
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? = inflater.inflate(R.layout.bottom_sheet_create_new_trans, container, false)


inner class Adapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            //expenses type ==1
            return ScreenSlidePageFragment(
                categoryList.filter { it.type == 1 },
                this@CreateNewTransBottomSheet
            )
        } else {
            //income type ==1
            return ScreenSlidePageFragment(
                categoryList.filter { it.type == 2 },
                this@CreateNewTransBottomSheet
            )

        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "Tab ${position + 1}"
    }
}

override fun onItemSelected(position: Int, item: Category) {
//        val action = SpecifyAmountFragmentDirections.confirmationAction(amount)
//        v.findNavController().navigate(action)
//        val action =
//            TransactionFragmentDirections.actionTransactionFragmentToCreateTransactionFragment(
//                categoryId = item.id
//            )
//        this.dismiss()
//        findNavController().navigate(action)

}

override fun restoreListPosition() {
}

/**
 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
 * sequence.
 */
private inner class ScreenSlidePagerAdapter(fa: Fragment) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        if (position == 0) {
            //expenses type ==1
            return ScreenSlidePageFragment(
                categoryList.filter { it.type == 1 },
                this@CreateNewTransBottomSheet
            )
        } else {
            //income type ==1
            return ScreenSlidePageFragment(
                categoryList.filter { it.type == 2 },
                this@CreateNewTransBottomSheet
            )

        }
    }
}
}*/