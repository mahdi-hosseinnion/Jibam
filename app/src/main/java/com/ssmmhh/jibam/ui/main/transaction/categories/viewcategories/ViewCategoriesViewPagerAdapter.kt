package com.ssmmhh.jibam.ui.main.transaction.categories.viewcategories

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.databinding.LayoutViewpagerListItemBinding
import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.util.Constants.EXPENSES_TYPE_MARKER
import com.ssmmhh.jibam.util.Constants.INCOME_TYPE_MARKER
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
            binding = LayoutViewpagerListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        if (position == 0) {
            //expenses
            //init recycler
            holder.binding.recyclerViewCategories.layoutManager = LinearLayoutManager(context)

            holder.binding.recyclerViewCategories.adapter = expensesRecyclerAdapter
            //attach touch helper for drag and drop reorder
            expensesItemTouchHelper.attachToRecyclerView(holder.binding.recyclerViewCategories)
        } else {
            //income
            //init recycler
            holder.binding.recyclerViewCategories.layoutManager = LinearLayoutManager(context)

            holder.binding.recyclerViewCategories.adapter = incomeRecyclerAdapter
            //attach touch helper for drag and drop reorder
            incomeItemTouchHelper.attachToRecyclerView(holder.binding.recyclerViewCategories)
        }

    }

    fun submitList(newData: List<Category>) {
        expensesRecyclerAdapter.submitData(newData.filter { it.type == EXPENSES_TYPE_MARKER })
        incomeRecyclerAdapter.submitData(newData.filter { it.type == INCOME_TYPE_MARKER })
    }

    override fun getItemCount(): Int = VIEWPAGER_SIZE

    class ViewPagerViewHolder(val binding: LayoutViewpagerListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val VIEWPAGER_SIZE = 2
    }
}