package com.ssmmhh.jibam.ui.main.transaction.addedittransaction.categorybottomsheet

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.util.CategoriesImageBackgroundColors
import kotlinx.android.synthetic.main.layout_category_images_list_item.view.*
import kotlinx.android.synthetic.main.layout_category_list_item.view.*
import kotlinx.android.synthetic.main.layout_category_list_item.view.category_image
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.*

class CategoryBottomSheetListAdapter(
    private val requestManager: RequestManager,
    private val interaction: Interaction? = null,
    private val packageName: String,
    private var selectedItemId: Int?
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
            packageName = packageName
        )
    }


    internal inner class BlogRecyclerChangeCallback(
        private val adapter: CategoryBottomSheetListAdapter
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
                holder.bind(differ.currentList.get(position), selectedItemId)
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
        val sortedList = blogList
        differ.submitList(sortedList)
    }

    fun submitSelectedId(id: Int?) {
        selectedItemId = id
        notifyDataSetChanged()
    }

    class CategoryViewHolder
    constructor(
        itemView: View,
        val requestManager: RequestManager,
        private val interaction: Interaction?,
        private val packageName: String
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Category, selectedItemId: Int?) = with(itemView) {
            itemView.setOnClickListener {

                setSelectedBackground(item.id)
                interaction?.onItemSelected(adapterPosition, item)
            }
            if (item.id == selectedItemId && item.id > 0) {
                setSelectedBackground(selectedItemId)
            } else {
                setUnSelectedBackground()
            }

            itemView.category_name.text =
                item.getCategoryNameFromStringFile(
                    resources,
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


        }


        private fun setSelectedBackground(categoryId: Int) {
            try {


                val circle_drawable = itemView.category_image_frame.background as Drawable

                circle_drawable?.setColorFilter(
                    itemView.resources.getColor(
                        CategoriesImageBackgroundColors.getCategoryColorById(categoryId)
                    ), PorterDuff.Mode.MULTIPLY
                )


                //change tint to white
                itemView.category_image.setColorFilter(
                    itemView.resources.getColor(R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } catch (e: Exception) {
                Log.e("CategoryViewHolder", "setSelectedBackground: message: ${e.message}", e)
            }
        }

        private fun setUnSelectedBackground() {
            // set to selected mode
/*            val circle_drawable = ResourcesCompat.getDrawable(
                itemView.resources,
                R.drawable.shape_category_item_circle,
                null
            )*/
//            itemView.category_image.setBackgroundColor(
//
//                itemView.resources.getColor(
//                    R.color.category_list_item_image_background_color
//                )
//            )
//            //change tint to black
//            itemView.category_image.setColorFilter(
//                itemView.resources.getColor(R.color.black),
//                android.graphics.PorterDuff.Mode.SRC_IN
//            )
        }

    }

    interface Interaction {

        fun onItemSelected(position: Int, item: Category)
    }
}