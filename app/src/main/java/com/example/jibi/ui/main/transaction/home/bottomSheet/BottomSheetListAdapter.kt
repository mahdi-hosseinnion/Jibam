package com.example.jibi.ui.main.transaction.home.bottomSheet

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.ui.main.transaction.transactions.TransactionsListAdapter
import com.example.jibi.util.sortCategoriesWithPinned
import kotlinx.android.synthetic.main.layout_category_images_list_item.view.*
import kotlinx.android.synthetic.main.layout_category_list_item.view.*
import kotlinx.android.synthetic.main.layout_category_list_item.view.category_image
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.*
import kotlin.random.Random

class BottomSheetListAdapter(
    private val requestManager: RequestManager,
    private val interaction: Interaction? = null,
    private val packageName: String,
    private val selectedCategoryId: Int,
    private val _resources: Resources
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG: String = "AppDebug"
    private val BLOG_ITEM = 0

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Category>() {

        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }

    }
    private val differ =
        AsyncListDiffer(
            BlogRecyclerChangeCallback(this),
            AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
        )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CategoryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_category_list_item,
                parent,
                false
            ),
            interaction = interaction,
            requestManager = requestManager,
            packageName = packageName,
            selectedCategoryId = selectedCategoryId,
            _resources = _resources
        )
    }


    internal inner class BlogRecyclerChangeCallback(
        private val adapter: BottomSheetListAdapter
    ) : ListUpdateCallback {

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position, count, payload)
        }

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeChanged(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList.get(position).id > -1) {
            return BLOG_ITEM
        }
        return differ.currentList.get(position).id
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    //    // Prepare the images that will be displayed in the RecyclerView.
//    // This also ensures if the network connection is lost, they will be in the cache
//    fun preloadGlideImages(
//        requestManager: RequestManager,
//        list: List<Category>
//    ){
//        for(Category in list){
//            requestManager
//                .load(Category.image)
//                .preload()
//        }
//    }
    fun setSelectedCategory(categoryId: Int) {
        var category1: Category? = null
        for (category in differ.currentList) {
            if (category.id == categoryId) {
                category1 = category
            }
        }
        category1?.let {
            notifyItemChanged(differ.currentList.indexOf(it))
        }
    }

    fun submitList(
        blogList: List<Category>?,
    ) {
        val sortedList = sortCategoriesWithPinned(blogList)
        differ.submitList(sortedList)
    }

    class CategoryViewHolder
    constructor(
        itemView: View,
        val requestManager: RequestManager,
        private val interaction: Interaction?,
        private val packageName: String,
        private val selectedCategoryId: Int,
        private val _resources: Resources
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Category) = with(itemView) {
            itemView.setOnClickListener {
                setSelectedBackground(item.id)
                interaction?.onItemSelected(adapterPosition, item)
            }
            if (item.id == selectedCategoryId && item.id > 0) {
                setSelectedBackground(selectedCategoryId)
            }

//            requestManager?.load(item.img_res)
//                ?.transition(withCrossFade())
//                ?.into(itemView.category_image)

            itemView.category_name.text =
                item.getCategoryNameFromStringFile(
                    _resources,
                    this@CategoryViewHolder.packageName
                ) {
                    it.name
                }
//            itemView.blog_update_date.text = DateUtils.convertLongToStringDate(item.date_updated)
            val categoryImageUrl = this.resources.getIdentifier(
                "ic_cat_${item.img_res}",
                "drawable",
                packageName
            )
            //TODO
//            itemView.card
            requestManager
                .load(categoryImageUrl)
                .centerInside()
                .transition(withCrossFade())
                .error(R.drawable.ic_error)
                .into(itemView.category_image)

            if (item.ordering < 0)
                itemView.pinned_marker_image.visibility = View.VISIBLE
            else
                itemView.pinned_marker_image.visibility = View.INVISIBLE
        }


        private fun setSelectedBackground(categoryId: Int) {
            // set to selected mode
            try {
                itemView.category_image.setBackgroundColor(

                    itemView.resources.getColor(
                        TransactionsListAdapter.listOfColor[(categoryId.minus(
                            1
                        ))]
                    )
                )
            } catch (e: Exception) {
                //apply random color
                itemView.category_image.setBackgroundColor(
                    itemView.resources.getColor(
                        TransactionsListAdapter.listOfColor[Random.nextInt(
                            TransactionsListAdapter.listOfColor.size
                        )]
                    )
                )
            }

            //change tint to white
            itemView.category_image.setColorFilter(
                itemView.resources.getColor(R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

    }

    interface Interaction {

        fun onItemSelected(position: Int, item: Category)
    }
}