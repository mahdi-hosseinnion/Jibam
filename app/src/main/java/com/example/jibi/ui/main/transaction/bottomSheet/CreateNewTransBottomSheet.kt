package com.example.jibi.ui.main.transaction.bottomSheet

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.load.engine.Resource
import com.example.jibi.R
import com.example.jibi.models.Category
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_create_new_trans.*


class CreateNewTransBottomSheet
constructor(
    private val categoryList: List<Category>
) : BottomSheetDialogFragment(),
    BottomSheetListAdapter.Interaction {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(this)

        bottom_sheet_viewPager.adapter = pagerAdapter
        bottom_sheet_viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    enableExpensesMode()
                } else {
                    enableIncomeMode()
                }
            }
        })
        txt_switch_expenses.setOnClickListener {
            enableExpensesMode()
        }
        txt_switch_income.setOnClickListener {
            enableIncomeMode()
        }

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
        this.dismiss()
        findNavController().navigate(R.id.action_transactionFragment_to_createTransactionFragment)

    }

    override fun restoreListPosition() {
    }


}