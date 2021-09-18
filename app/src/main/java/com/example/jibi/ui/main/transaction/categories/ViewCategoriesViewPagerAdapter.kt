package com.example.jibi.ui.main.transaction.categories

import android.content.Context
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
import com.example.jibi.util.Constants.EXPENSES_TYPE_MARKER
import com.example.jibi.util.Constants.INCOME_TYPE_MARKER
import kotlinx.android.synthetic.main.layout_viewpager_list_item.view.*
import kotlinx.coroutines.FlowPreview

@FlowPreview
class ViewCategoriesViewPagerAdapter(
    private val context: Context,
    private var expensesItemTouchHelper: ItemTouchHelper,
    private var incomeItemTouchHelper: ItemTouchHelper,
    listOfCategories: List<Category>? = null,
    categoryInteraction: ViewCategoriesRecyclerAdapter.CategoryInteraction,
    requestManager: RequestManager,
    packageName: String
) : RecyclerView.Adapter<ViewCategoriesViewPagerAdapter.ViewPagerViewHolder>() {

    private val expensesRecyclerAdapter: ViewCategoriesRecyclerAdapter =
        ViewCategoriesRecyclerAdapter(
            listOfCategories = listOfCategories?.filter { it.type == EXPENSES_TYPE_MARKER },
            interaction = categoryInteraction,
            requestManager = requestManager,
            packageName = packageName

        )

    private val incomeRecyclerAdapter: ViewCategoriesRecyclerAdapter =
        ViewCategoriesRecyclerAdapter(
            listOfCategories = listOfCategories?.filter { it.type == INCOME_TYPE_MARKER },
            interaction = categoryInteraction,
            requestManager = requestManager,
            packageName = packageName

        )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder =
        ViewPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_viewpager_list_item,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        if (position == 0) {
            //expenses
            //init recycler
            holder.itemView.recycler_viewCategories.layoutManager = LinearLayoutManager(context)

            holder.itemView.recycler_viewCategories.adapter = expensesRecyclerAdapter
            //attach touch helper for drag and drop reorder
            expensesItemTouchHelper.attachToRecyclerView(holder.itemView.recycler_viewCategories)
        } else {
            //income
            //init recycler
            holder.itemView.recycler_viewCategories.layoutManager = LinearLayoutManager(context)

            holder.itemView.recycler_viewCategories.adapter = incomeRecyclerAdapter
            //attach touch helper for drag and drop reorder
            incomeItemTouchHelper.attachToRecyclerView(holder.itemView.recycler_viewCategories)
        }

    }

    fun submitList(newData: List<Category>) {
        expensesRecyclerAdapter.submitData(newData.filter { it.type == EXPENSES_TYPE_MARKER })
        incomeRecyclerAdapter.submitData(newData.filter { it.type == INCOME_TYPE_MARKER })
    }

    override fun getItemCount(): Int = VIEWPAGER_SIZE

    class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        private const val VIEWPAGER_SIZE = 2
    }
}