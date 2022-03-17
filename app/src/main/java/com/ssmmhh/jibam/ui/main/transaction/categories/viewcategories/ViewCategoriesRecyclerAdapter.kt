package com.ssmmhh.jibam.ui.main.transaction.categories.viewcategories

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.LayoutViewCategoriesListItemBinding
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.util.CategoriesImageBackgroundColors
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.indices
import kotlin.collections.set

class ViewCategoriesRecyclerAdapter(
    private var listOfCategoryEntities: List<CategoryEntity>?,
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

    fun getItemAtPosition(position: Int): CategoryEntity? = listOfCategoryEntities?.get(position)

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

    fun submitData(data: List<CategoryEntity>?) {
        listOfCategoryEntities = data
        notifyDataSetChanged()
    }

    class ViewPagerRecyclerViewHolder(
        val binding: LayoutViewCategoriesListItemBinding,
        private val categoryInteraction: CategoryInteraction,
        private val requestManager: RequestManager,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(holder: RecyclerView.ViewHolder, item: CategoryEntity?) {
            if (item != null) {
                //todo fix show promote
//                Check that the view exists for the item
//                if (adapterPosition == 0 &&
//                    sharedPreferences.getBoolean(
//                        PreferenceKeys.PROMOTE_VIEW_CATEGORY_LIST,
//                        true
//                    )
//                ) {
//
//                    showPromote()
//                }
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
                    //FOR DEBUG
//                    itemView.ordering.text = item.ordering.toString()
//                    itemView.category_id.text = item.id.toString()
//                    val color = if (item.ordering == adapterPosition) Color.BLACK else Color.RED
//                    itemView.ordering.setTextColor(color)

                    binding.cardViewViewCategory.setCardBackgroundColor(
                        resources.getColor(
                            CategoriesImageBackgroundColors.getCategoryColorById(item.id)

                        )
                    )

                    val categoryImageResourceId = item.getCategoryImageResourceId(context)
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

        //todo fix show promote
//        private fun showPromote(context: Context) {
//            MaterialTapTargetPrompt.Builder(context)
//                .setTarget(itemView.findViewById(R.id.root_transaction_item))
//                .setPrimaryText(_getString(R.string.view_category_tap_target_primary))
//                .setSecondaryText(_getString(R.string.view_category_tap_target_secondary))
//                .setPromptBackground(RectanglePromptBackground())
//                .setPromptFocal(RectanglePromptFocal())
//                .setPromptStateChangeListener { _, state ->
//                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
//                        sharedPrefsEditor.putBoolean(
//                            PreferenceKeys.PROMOTE_VIEW_CATEGORY_LIST,
//                            false
//                        ).apply()
//                    }
//                }
//                .show()
//        }
    }

    interface CategoryInteraction {
        fun onDeleteClicked(position: Int, categoryEntity: CategoryEntity)

        //called when ad view request a start of drag
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder, itemType: Int)
    }

}