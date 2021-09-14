package com.example.jibi.ui.main.transaction.addedittransaction.categorybottomsheet

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.util.Constants.EXPENSES_TYPE_MARKER
import com.example.jibi.util.Constants.INCOME_TYPE_MARKER


class CategoryBottomSheetViewPagerAdapter(
    private val context: Context,
    private val categoryList: List<Category>?,
    private var isLeftToRight: Boolean,
    private val _resources: Resources,
    interaction: CategoryBottomSheetListAdapter.Interaction? = null,
    requestManager: RequestManager,
    packageName: String,
    selectedCategoryId: Int?
) : PagerAdapter() {
    private val expensesRecyclerViewAdapter = CategoryBottomSheetListAdapter(
        requestManager,
        interaction,
        packageName,
        selectedItemId = selectedCategoryId,
        _resources = _resources
    )
    private val incomeRecyclerViewAdapter = CategoryBottomSheetListAdapter(
        requestManager,
        interaction,
        packageName,
        selectedItemId = selectedCategoryId,
        _resources = _resources
    )

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.recycler_view_layout, container, false)
        //support rtl
        if (isLeftToRight) {
            if (position == 0) {
                //expenses type ==1
                initRecycler(view.findViewById(R.id.main_recycler), expensesRecyclerViewAdapter)
                expensesRecyclerViewAdapter.submitList(categoryList?.filter { it.type == EXPENSES_TYPE_MARKER })
            } else {
                //income type ==2
                initRecycler(view.findViewById(R.id.main_recycler), incomeRecyclerViewAdapter)
                incomeRecyclerViewAdapter.submitList(categoryList?.filter { it.type == INCOME_TYPE_MARKER })
            }
        } else {
            if (position == 0) {
                //income type ==2
                initRecycler(view.findViewById(R.id.main_recycler), incomeRecyclerViewAdapter)
                incomeRecyclerViewAdapter.submitList(categoryList?.filter { it.type == INCOME_TYPE_MARKER })
            } else {
                //expenses type ==1
                initRecycler(view.findViewById(R.id.main_recycler), expensesRecyclerViewAdapter)
                expensesRecyclerViewAdapter.submitList(categoryList?.filter { it.type == EXPENSES_TYPE_MARKER })
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
            layoutManager = GridLayoutManager(context, 4)
//            isNestedScrollingEnabled = false
            adapter = recyclerAdapter
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int = VIEW_PAGER_SIZE

    override fun isViewFromObject(view: View, `object`: Any): Boolean = (`object` == view)


    override fun getPageTitle(position: Int): CharSequence {
        val expenses = _resources.getString(R.string.expenses)
        val income = _resources.getString(R.string.income)
        return if (isLeftToRight)
            if (position == 0) expenses else income
        else
            if (position == 0) income else expenses
    }

    fun submitData(categoryList: List<Category>?) {

        expensesRecyclerViewAdapter.submitList(categoryList?.filter { it.type == EXPENSES_TYPE_MARKER })
        incomeRecyclerViewAdapter.submitList(categoryList?.filter { it.type == INCOME_TYPE_MARKER })
    }

    fun submitSelectedItemId(id: Int?) {

        expensesRecyclerViewAdapter.submitSelectedId(id)
        incomeRecyclerViewAdapter.submitSelectedId(id)
    }

    companion object {
        const val VIEW_PAGER_SIZE = 2

    }
}