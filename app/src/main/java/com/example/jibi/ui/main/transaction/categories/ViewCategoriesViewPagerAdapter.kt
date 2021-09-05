package com.example.jibi.ui.main.transaction.categories

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.models.Category
import kotlinx.android.synthetic.main.layout_viewpager_list_item.view.*
import kotlinx.coroutines.FlowPreview

@FlowPreview
class ViewCategoriesViewPagerAdapter(
    private var listOfCategories: List<Category>? = null,
    private var expensesItemTouchHelper: ItemTouchHelper,
    private var incomeItemTouchHelper: ItemTouchHelper,
    private var categoryInteraction: ViewCategoriesRecyclerAdapter.CategoryInteraction,
    private val _resources: Resources,
    private val requestManager: RequestManager,
    private val packageName: String,
    private var viewPagerSize: Int = VIEWPAGER_SIZE
) : RecyclerView.Adapter<ViewCategoriesViewPagerAdapter.ViewPagerViewHolder>() {

    private var currentPage = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder =
        ViewPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_viewpager_list_item,
                parent,
                false
            ),
            categoryInteraction = categoryInteraction,
            _resources = _resources,
            requestManager = requestManager,
            packageName = packageName
        )

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        if (position == 0) {
            //expenses
            holder.bind(listOfCategories?.filter { category -> category.type == 1 })
            expensesItemTouchHelper.attachToRecyclerView(holder.itemView.recycler_viewCategories)
        } else {
            //income
            holder.bind(listOfCategories?.filter { category -> category.type > 1 })
            incomeItemTouchHelper.attachToRecyclerView(holder.itemView.recycler_viewCategories)
        }

    }

    fun submitList(listOfCategories: List<Category>) {
        this.listOfCategories = listOfCategories
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = viewPagerSize


    class ViewPagerViewHolder(
        itemView: View,
        private val categoryInteraction: ViewCategoriesRecyclerAdapter.CategoryInteraction,
        private val _resources: Resources,
        private val requestManager: RequestManager,
        private val packageName: String
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(categoryList: List<Category>?) {

            itemView.recycler_viewCategories.apply {

                layoutManager = LinearLayoutManager(this.context)

                adapter = ViewCategoriesRecyclerAdapter(
                    listOfCategories = categoryList,
                    interaction = categoryInteraction,
                    _resources = _resources,
                    requestManager = requestManager,
                    packageName = packageName

                )
            }

        }
    }


    fun getCurrentPage(): Int {
        return this.currentPage
    }

    companion object {
        const val VIEWPAGER_SIZE = 2
    }
}