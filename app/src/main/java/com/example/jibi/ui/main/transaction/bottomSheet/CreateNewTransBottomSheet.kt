package com.example.jibi.ui.main.transaction.bottomSheet

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.load.engine.Resource
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.ui.main.transaction.TransactionFragment
import com.example.jibi.ui.main.transaction.TransactionFragmentDirections
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_create_new_trans.*
import kotlinx.android.synthetic.main.fragment_screen_slide_page.*


class CreateNewTransBottomSheet
constructor(
    private val categoryList: List<Category>
) : BottomSheetDialogFragment(),
    BottomSheetListAdapter.Interaction {

    private lateinit var recyclerAdapter: BottomSheetListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // The pager adapter, which provides the pages to the view pager widget.
        initRecyclerView()
//        val pagerAdapter = ScreenSlidePagerAdapter(this)
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
        fakeRecycler.apply {
            layoutManager = GridLayoutManager(this@CreateNewTransBottomSheet.context, 4)
            recyclerAdapter = BottomSheetListAdapter(
                null,
                null
            )
            adapter = recyclerAdapter
        }
        recyclerAdapter.submitList(categoryList)
        fakeRecycler.isNestedScrollingEnabled = false
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_create_new_trans, container, false)


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

    override fun onItemSelected(position: Int, item: Category) {
//        val action = SpecifyAmountFragmentDirections.confirmationAction(amount)
//        v.findNavController().navigate(action)
        val action = TransactionFragmentDirections.actionTransactionFragmentToCreateTransactionFragment(categoryId = item.id)
        this.dismiss()
        findNavController().navigate(action)

    }

    override fun restoreListPosition() {
    }


}