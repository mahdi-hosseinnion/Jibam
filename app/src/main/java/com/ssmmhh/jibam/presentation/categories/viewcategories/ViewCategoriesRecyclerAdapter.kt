package com.ssmmhh.jibam.presentation.categories.viewcategories

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.LayoutViewCategoriesListItemBinding
import com.ssmmhh.jibam.data.model.Category
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.indices
import kotlin.collections.set

@FlowPreview
@ExperimentalCoroutinesApi
class ViewCategoriesRecyclerAdapter
constructor(
    private var listOfCategoryEntities: List<Category>? = null,
    private val viewModel: ViewCategoriesViewModel,
    private val requestManager: RequestManager,
    private val startDragOnViewHolder: (viewHolder: RecyclerView.ViewHolder) -> Unit,
) : RecyclerView.Adapter<ViewCategoriesRecyclerAdapter.ViewPagerRecyclerViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewPagerRecyclerViewHolder =
        ViewPagerRecyclerViewHolder(
            binding = LayoutViewCategoriesListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            requestManager = requestManager,
            startDragOnViewHolder = startDragOnViewHolder,
        )

    override fun onBindViewHolder(holder: ViewPagerRecyclerViewHolder, position: Int) {
        listOfCategoryEntities?.getOrNull(position)?.let { category ->
            holder.bind(viewModel, holder, category)
        }
    }

    override fun getItemCount(): Int = listOfCategoryEntities?.size ?: 0

    fun getItemAtPosition(position: Int): Category? = listOfCategoryEntities?.get(position)

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
        val binding: LayoutViewCategoriesListItemBinding,
        private val requestManager: RequestManager,
        private val startDragOnViewHolder: (viewHolder: RecyclerView.ViewHolder) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            viewModel: ViewCategoriesViewModel,
            holder: RecyclerView.ViewHolder,
            category: Category
        ) = with(binding) {
            viewmodel = viewModel
            item = category
            changeCategoryOrderHandle.setOnTouchListener { view, motionEvent ->
                if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                    startDragOnViewHolder(holder)
                }
                return@setOnTouchListener false
            }
            val categoryName = category.getCategoryNameFromStringFile(itemView.context)
            nameOfCategory.text = categoryName

            cardViewViewCategory.setCardBackgroundColor(
                Color.parseColor(category.image.backgroundColor)
            )

            val categoryImageResourceId = category.image.getImageResourceId(itemView.context)

            requestManager
                .load(categoryImageResourceId)
                .centerInside()
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.ic_error)
                .into(categoryImage)


        }

    }


}