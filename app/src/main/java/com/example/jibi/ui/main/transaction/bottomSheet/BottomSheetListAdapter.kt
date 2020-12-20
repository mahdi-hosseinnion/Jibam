package com.example.jibi.ui.main.transaction.bottomSheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.jibi.R
import com.example.jibi.models.Category
import kotlinx.android.synthetic.main.layout_category_list_item.view.*

class BottomSheetListAdapter(
    private val requestManager: RequestManager?,
    private val interaction: Interaction? = null
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
            requestManager = requestManager
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

    fun submitList(
        blogList: List<Category>?,
    ) {
        val commitCallback = Runnable {
            // if process died must restore list position
            // very annoying
            interaction?.restoreListPosition()
        }
        differ.submitList(blogList, commitCallback)
    }

    class CategoryViewHolder
    constructor(
        itemView: View,
        val requestManager: RequestManager?,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Category) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            requestManager?.load(item.img_res)
                ?.transition(withCrossFade())
                ?.into(itemView.category_image)
            itemView.category_name.text = item.name
//            itemView.blog_update_date.text = DateUtils.convertLongToStringDate(item.date_updated)
        }
    }

    interface Interaction {

        fun onItemSelected(position: Int, item: Category)

        fun restoreListPosition()
    }
}