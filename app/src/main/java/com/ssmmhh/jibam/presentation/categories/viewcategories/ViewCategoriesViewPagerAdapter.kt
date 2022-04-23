package com.ssmmhh.jibam.presentation.categories.viewcategories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.databinding.LayoutViewpagerListItemBinding
import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class ViewCategoriesViewPagerAdapter(
    private val viewModel: ViewCategoriesViewModel,
    requestManager: RequestManager,
) : RecyclerView.Adapter<ViewCategoriesViewPagerAdapter.ViewPagerViewHolder>() {

    private val expensesRecyclerAdapter: ViewCategoriesRecyclerAdapter by lazy {
        ViewCategoriesRecyclerAdapter(
            viewModel = viewModel,
            requestManager = requestManager,
            startDragOnViewHolder = { viewHolder ->
                expensesItemTouchHelper.startDrag(viewHolder)
            }
        )
    }

    private val incomeRecyclerAdapter: ViewCategoriesRecyclerAdapter by lazy {
        ViewCategoriesRecyclerAdapter(
            viewModel = viewModel,
            requestManager = requestManager,
            startDragOnViewHolder = { viewHolder ->
                incomeItemTouchHelper.startDrag(viewHolder)
            }
        )
    }

    private val expensesItemTouchHelper by lazy {
        ItemTouchHelper(ViewCategoryItemTouchHelperCallback {
            viewModel.newReorder(it, CategoryEntity.EXPENSES_TYPE_MARKER)
        })
    }
    private val incomeItemTouchHelper by lazy {
        ItemTouchHelper(ViewCategoryItemTouchHelperCallback {
            viewModel.newReorder(it, CategoryEntity.INCOME_TYPE_MARKER)
        })
    }

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
            holder.bind(
                adapter = expensesRecyclerAdapter,
                itemTouchHelper = expensesItemTouchHelper
            )
        } else if (position == 1) {
            holder.bind(
                adapter = incomeRecyclerAdapter,
                itemTouchHelper = incomeItemTouchHelper
            )
        }

    }

    fun submitExpensesCategoryList(newData: List<Category>) {
        expensesRecyclerAdapter.submitData(newData)
    }

    fun submitIncomeCategoryList(newData: List<Category>) {
        incomeRecyclerAdapter.submitData(newData)
    }

    override fun getItemCount(): Int = VIEWPAGER_SIZE

    class ViewPagerViewHolder(
        val binding: LayoutViewpagerListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            adapter: ViewCategoriesRecyclerAdapter,
            itemTouchHelper: ItemTouchHelper,
        ) = with(binding) {
            recyclerViewCategories.adapter = adapter
            //attach touch helper for drag and drop reorder
            itemTouchHelper.attachToRecyclerView(recyclerViewCategories)
        }
    }

    companion object {
        private const val VIEWPAGER_SIZE = 2
    }
}