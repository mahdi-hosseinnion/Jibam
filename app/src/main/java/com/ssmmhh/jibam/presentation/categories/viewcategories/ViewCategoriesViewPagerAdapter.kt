package com.ssmmhh.jibam.presentation.categories.viewcategories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.databinding.LayoutViewpagerListItemBinding
import com.ssmmhh.jibam.data.model.Category
import kotlinx.coroutines.FlowPreview

@FlowPreview
class ViewCategoriesViewPagerAdapter(
    private var expensesItemTouchHelper: ItemTouchHelper,
    private var incomeItemTouchHelper: ItemTouchHelper,
    listOfCategoryEntities: List<Category>? = null,
    categoryInteraction: ViewCategoriesRecyclerAdapter.CategoryInteraction,
    requestManager: RequestManager,
) : RecyclerView.Adapter<ViewCategoriesViewPagerAdapter.ViewPagerViewHolder>() {

    private val expensesRecyclerAdapter: ViewCategoriesRecyclerAdapter =
        ViewCategoriesRecyclerAdapter(
            listOfCategoryEntities = listOfCategoryEntities?.filter { it.isExpensesCategory },
            interaction = categoryInteraction,
            requestManager = requestManager,
        )

    private val incomeRecyclerAdapter: ViewCategoriesRecyclerAdapter =
        ViewCategoriesRecyclerAdapter(
            listOfCategoryEntities = listOfCategoryEntities?.filter { it.isIncomeCategory },
            interaction = categoryInteraction,
            requestManager = requestManager,
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
            holder.binding.recyclerViewCategories.adapter = expensesRecyclerAdapter
            //attach touch helper for drag and drop reorder
            expensesItemTouchHelper.attachToRecyclerView(holder.binding.recyclerViewCategories)
        } else {
            //income
            //init recycler
            holder.binding.recyclerViewCategories.adapter = incomeRecyclerAdapter
            //attach touch helper for drag and drop reorder
            incomeItemTouchHelper.attachToRecyclerView(holder.binding.recyclerViewCategories)
        }

    }

    fun submitExpensesCategoryList(newData: List<Category>) {
        expensesRecyclerAdapter.submitData(newData)
    }

    fun submitIncomeCategoryList(newData: List<Category>) {
        incomeRecyclerAdapter.submitData(newData)
    }

    override fun getItemCount(): Int = VIEWPAGER_SIZE

    class ViewPagerViewHolder(val binding: LayoutViewpagerListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val VIEWPAGER_SIZE = 2
    }
}