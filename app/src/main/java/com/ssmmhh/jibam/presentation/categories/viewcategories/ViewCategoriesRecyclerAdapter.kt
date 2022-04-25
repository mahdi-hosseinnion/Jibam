package com.ssmmhh.jibam.presentation.categories.viewcategories

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.databinding.LayoutViewCategoriesListItemBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlin.collections.set

@FlowPreview
@ExperimentalCoroutinesApi
class ViewCategoriesRecyclerAdapter
constructor(
    private var listOfCategoryEntities: List<Category>? = null,
    private val viewModel: ViewCategoriesViewModel,
    private val startDragOnViewHolder: (viewHolder: RecyclerView.ViewHolder) -> Unit,
) : RecyclerView.Adapter<ViewCategoriesRecyclerAdapter.ViewPagerRecyclerViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewPagerRecyclerViewHolder = ViewPagerRecyclerViewHolder(
        binding = LayoutViewCategoriesListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ),
        startDragOnViewHolder = startDragOnViewHolder,
    )

    override fun onBindViewHolder(holder: ViewPagerRecyclerViewHolder, position: Int) {
        listOfCategoryEntities?.getOrNull(position)?.let { category ->
            holder.bind(viewModel, category)
        }
    }

    override fun getItemCount(): Int = listOfCategoryEntities?.size ?: 0

    private fun getItemAtPosition(position: Int): Category? =
        listOfCategoryEntities?.getOrNull(position)

    fun onItemMoved(from: Int, to: Int) {
        listOfCategoryEntities?.let { list ->
            if (from == to) {
                return
            }
            //TODO ("ui test this")
            val fromObj = getItemAtPosition(from)
            val categories = ArrayList(list)
            //change position of main object(moved object)
            categories.removeAt(from)
            categories.add(to, fromObj)
            listOfCategoryEntities = categories
        }
    }

    fun getOrder(): HashMap<Int, Int> {
        val result = HashMap<Int, Int>()
        listOfCategoryEntities?.let { categories ->
            for (i in categories.indices) {
                result[categories[i].id] = i
            }
        }

        return result
    }

    fun submitData(data: List<Category>?) {
        listOfCategoryEntities = data
        notifyDataSetChanged()
    }

    class ViewPagerRecyclerViewHolder(
        private val binding: LayoutViewCategoriesListItemBinding,
        private val startDragOnViewHolder: (viewHolder: RecyclerView.ViewHolder) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            viewModel: ViewCategoriesViewModel,
            category: Category
        ) = with(binding) {
            viewmodel = viewModel
            item = category
            executePendingBindings()
            changeCategoryOrderHandle.setOnTouchListener { view, motionEvent ->
                if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                    startDragOnViewHolder(this@ViewPagerRecyclerViewHolder)
                }
                return@setOnTouchListener false
            }
        }

    }


}