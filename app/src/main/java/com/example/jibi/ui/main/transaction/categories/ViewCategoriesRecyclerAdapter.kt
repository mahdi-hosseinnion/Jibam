package com.example.jibi.ui.main.transaction.categories

import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.ui.main.transaction.transactions.TransactionsListAdapter
import kotlinx.android.synthetic.main.layout_view_categories_list_item.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.indices
import kotlin.collections.set
import kotlin.random.Random

class ViewCategoriesRecyclerAdapter(
    private var listOfCategories: List<Category>?,
    private val interaction: CategoryInteraction,
    private val requestManager: RequestManager,
    private val packageName: String
) : RecyclerView.Adapter<ViewCategoriesRecyclerAdapter.ViewPagerRecyclerViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewPagerRecyclerViewHolder =
        ViewPagerRecyclerViewHolder(
            itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.layout_view_categories_list_item,
                parent,
                false
            ),
            categoryInteraction = interaction,
            requestManager = requestManager,
            packageName = packageName
        )

    override fun onBindViewHolder(holder: ViewPagerRecyclerViewHolder, position: Int) {
        holder.bind(holder, listOfCategories?.get(position))
    }

    override fun getItemCount(): Int = listOfCategories?.size ?: 0

    fun getItemAtPosition(position: Int): Category? = listOfCategories?.get(position)

    fun onItemMoved(from: Int, to: Int) {
        listOfCategories?.let { list ->
            if (from == to) {
                return
            }
            //TODO BUG KHIZE IF THIIS CALLED AFTER GETORDER
            val fromObj = getItemAtPosition(from)
            val categories = ArrayList(list)
            //change position of main object(moved object)
            categories.removeAt(from)
            categories.add(to, fromObj)
            listOfCategories = categories
        }
    }

    fun getOrder(): HashMap<Int, Int> {
        val result = HashMap<Int, Int>()
        listOfCategories?.let { categories ->
            for (i in categories.indices) {
                result[categories[i].id] = i
            }
        }

        return result
    }

    fun submitData(data: List<Category>?) {
        listOfCategories = data
        notifyDataSetChanged()
    }

    class ViewPagerRecyclerViewHolder(
        itemView: View,
        private val categoryInteraction: CategoryInteraction,
        private val requestManager: RequestManager,
        private val packageName: String
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(holder: RecyclerView.ViewHolder, item: Category?) {
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

                    itemView.change_category_order_handle.setOnTouchListener { view, motionEvent ->
                        if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                            categoryInteraction.onStartDrag(holder, item.type)
                        }
                        performClick()
                        return@setOnTouchListener false
                    }
                    val categoryName = item.getCategoryNameFromStringFile(
                        resources,
                        packageName
                    ) {
                        it.name
                    }
                    itemView.nameOfCategory.text = categoryName
                    //FOR DEBUG
//                    itemView.ordering.text = item.ordering.toString()
//                    itemView.category_id.text = item.id.toString()
//                    val color = if (item.ordering == adapterPosition) Color.BLACK else Color.RED
//                    itemView.ordering.setTextColor(color)

                    if (item.id > 0) {
                        try {
                            cardView_view_category.setCardBackgroundColor(
                                resources.getColor(
                                    TransactionsListAdapter.listOfColor[(item.id.minus(
                                        1
                                    ))]
                                )
                            )
                        } catch (e: Exception) {
                            //apply random color
                            cardView_view_category.setCardBackgroundColor(
                                resources.getColor(
                                    TransactionsListAdapter.listOfColor[Random.nextInt(
                                        TransactionsListAdapter.listOfColor.size
                                    )]
                                )
                            )
                        }
                    }

                    val categoryImageUrl = this.resources.getIdentifier(
                        "ic_cat_${item.img_res}",
                        "drawable",
                        packageName
                    )
                    requestManager
                        .load(categoryImageUrl)
                        .centerInside()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.ic_error)
                        .into(itemView.category_image)

                    delete_category.setOnClickListener {
                        categoryInteraction.onDeleteClicked(adapterPosition, item)
                    }
                }
            } else {
                itemView.nameOfCategory.text =
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
        fun onDeleteClicked(position: Int, category: Category)

        //called when ad view request a start of drag
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder, itemType: Int)
    }

}