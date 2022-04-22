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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.indices
import kotlin.collections.set

//TODO ("Add diff util to make sure there is no lag in recycler view while reordering the list
// especially from bottom to top")
class ViewCategoriesRecyclerAdapter(
    private var listOfCategoryEntities: List<Category>?,
    private val interaction: CategoryInteraction,
    private val requestManager: RequestManager,
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
            categoryInteraction = interaction,
            requestManager = requestManager,
        )

    override fun onBindViewHolder(holder: ViewPagerRecyclerViewHolder, position: Int) {
        holder.bind(holder, listOfCategoryEntities?.get(position))
    }

    override fun getItemCount(): Int = listOfCategoryEntities?.size ?: 0

    fun getItemAtPosition(position: Int): Category? = listOfCategoryEntities?.get(position)

    fun onItemMoved(from: Int, to: Int) {
        listOfCategoryEntities?.let { list ->
            if (from == to) {
                return
            }
            //TODO BUG KHIZE IF THIIS CALLED AFTER GETORDER
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
        private val categoryInteraction: CategoryInteraction,
        private val requestManager: RequestManager,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(holder: RecyclerView.ViewHolder, item: Category?) {
            if (item != null) {
                itemView.apply {
                    binding.changeCategoryOrderHandle.setOnTouchListener { view, motionEvent ->
                        if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                            categoryInteraction.onStartDrag(holder, item.type)
                        }
                        performClick()
                        return@setOnTouchListener false
                    }
                    val categoryName = item.getCategoryNameFromStringFile(context)
                    binding.nameOfCategory.text = categoryName

                    binding.cardViewViewCategory.setCardBackgroundColor(
                        Color.parseColor(item.image.backgroundColor)
                    )

                    val categoryImageResourceId = item.image.getImageResourceId(context)

                    requestManager
                        .load(categoryImageResourceId)
                        .centerInside()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.ic_error)
                        .into(binding.categoryImage)

                    binding.deleteCategory.setOnClickListener {
                        categoryInteraction.onDeleteClicked(adapterPosition, item)
                    }
                }
            } else {
                binding.nameOfCategory.text =
                    itemView.resources.getString(R.string.UNKNOWN_CATEGORY)
            }
        }

    }

    interface CategoryInteraction {
        fun onDeleteClicked(position: Int, categoryEntity: Category)

        //called when ad view request a start of drag
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder, itemType: Int)
    }

}