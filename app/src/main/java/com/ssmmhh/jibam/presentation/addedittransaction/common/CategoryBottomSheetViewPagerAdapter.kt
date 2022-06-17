package com.ssmmhh.jibam.presentation.addedittransaction.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.Category

class CategoryBottomSheetViewPagerAdapter(
    private val context: Context,
    private val categoryEntityList: List<Category>?,
    private var isLeftToRight: Boolean,
    interaction: CategoryBottomSheetListAdapter.Interaction? = null,
    requestManager: RequestManager,
    selectedCategoryId: Int?
) : PagerAdapter() {

    private val expensesRecyclerViewAdapter: CategoryBottomSheetListAdapter =
        CategoryBottomSheetListAdapter(
            requestManager,
            interaction,
            selectedItemId = selectedCategoryId,
            onItemSelected = {
                incomeRecyclerViewAdapter.deselectSelectedItem()
            }
        )
    private val incomeRecyclerViewAdapter: CategoryBottomSheetListAdapter =
        CategoryBottomSheetListAdapter(
            requestManager,
            interaction,
            selectedItemId = selectedCategoryId,
            onItemSelected = {
                expensesRecyclerViewAdapter.deselectSelectedItem()
            }
        )

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.recycler_view_layout, container, false)
        //support rtl
        if (isLeftToRight) {
            if (position == 0) {
                //expenses type ==1
                initRecycler(view.findViewById(R.id.main_recycler), expensesRecyclerViewAdapter)
                if (categoryEntityList != null)
                    expensesRecyclerViewAdapter.submitList(categoryEntityList.filter { it.isExpensesCategory })
            } else {
                //income type ==2
                initRecycler(view.findViewById(R.id.main_recycler), incomeRecyclerViewAdapter)
                if (categoryEntityList != null)
                    incomeRecyclerViewAdapter.submitList(categoryEntityList.filter { it.isIncomeCategory })
            }
        } else {
            if (position == 0) {
                //income type ==2
                initRecycler(view.findViewById(R.id.main_recycler), incomeRecyclerViewAdapter)
                if (categoryEntityList != null)
                    incomeRecyclerViewAdapter.submitList(categoryEntityList.filter { it.isIncomeCategory })
            } else {
                //expenses type ==1
                initRecycler(view.findViewById(R.id.main_recycler), expensesRecyclerViewAdapter)
                if (categoryEntityList != null)
                    expensesRecyclerViewAdapter.submitList(categoryEntityList.filter { it.isExpensesCategory })
            }
        }
        container.addView(view)
        return view
    }

    private fun initRecycler(
        recycler: RecyclerView,
        recyclerAdapter: CategoryBottomSheetListAdapter
    ) {
        recycler.apply {
            layoutManager = GridLayoutManager(context, 5)
            adapter = recyclerAdapter
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int = VIEW_PAGER_SIZE

    override fun isViewFromObject(view: View, `object`: Any): Boolean = (`object` == view)


    override fun getPageTitle(position: Int): CharSequence {
        val expenses = this.context.resources.getString(R.string.expenses)
        val income = this.context.resources.getString(R.string.income)
        return if (isLeftToRight)
            if (position == 0) expenses else income
        else
            if (position == 0) income else expenses
    }

    fun submitData(categoryEntityList: List<Category>?) {

        expensesRecyclerViewAdapter.submitList(categoryEntityList?.filter { it.isExpensesCategory })
        incomeRecyclerViewAdapter.submitList(categoryEntityList?.filter { it.isIncomeCategory })
    }

    fun submitSelectedItemId(id: Int?) {}

    companion object {
        const val VIEW_PAGER_SIZE = 2

    }
}