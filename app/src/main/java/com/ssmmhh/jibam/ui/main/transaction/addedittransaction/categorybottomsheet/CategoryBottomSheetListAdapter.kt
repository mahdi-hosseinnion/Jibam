package com.ssmmhh.jibam.ui.main.transaction.addedittransaction.categorybottomsheet

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.LayoutCategoryListItemBinding
import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.util.CategoriesImageBackgroundColors
import com.ssmmhh.jibam.util.EspressoIdlingResources

//TODO REMOVE DIFF FROM THIS ADAPTER
class CategoryBottomSheetListAdapter(
    private val requestManager: RequestManager,
    private val interaction: Interaction? = null,
    private var selectedItemId: Int?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG: String = "CategoryBottomSheetListAdapter"
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
        val binding = LayoutCategoryListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(
            binding = binding,
            interaction = interaction,
            requestManager = requestManager,
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
        var categoryEntity1: Category? = null
        for (category in differ.currentList) {
            if (category.id == categoryId) {
                categoryEntity1 = category
            }
        }
        categoryEntity1?.let {
            notifyItemChanged(differ.currentList.indexOf(it))
        }
    }

    fun submitList(
        blogList: List<Category>?,
    ) {
        EspressoIdlingResources.increment(TAG)
        //after diffUtil library do its calculation then this runnable will run
        val commitCallback = Runnable {
            EspressoIdlingResources.decrement(TAG)
        }
        differ.submitList(blogList, commitCallback)
    }

    fun submitSelectedId(id: Int?) {
        selectedItemId = id
        notifyDataSetChanged()
    }

    class CategoryViewHolder
    constructor(
        val binding: LayoutCategoryListItemBinding,
        val requestManager: RequestManager,
        private val interaction: Interaction?,
    ) : RecyclerView.ViewHolder(binding.root) {

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

            binding.categoryName.text = item.getCategoryNameFromStringFile(context)

            val categoryImageResourceId = item.image.getImageResourceId(context)
            requestManager
                .load(categoryImageResourceId)
                .centerInside()
                .transition(withCrossFade())
                .error(R.drawable.ic_error)
                .into(binding.categoryImage)


        }


        private fun setSelectedBackground(categoryId: Int) {
            try {


                val circle_drawable = binding.categoryImageFrame.background as Drawable

                circle_drawable?.setColorFilter(
                    itemView.resources.getColor(
                        CategoriesImageBackgroundColors.getCategoryColorById(categoryId)
                    ), PorterDuff.Mode.MULTIPLY
                )


                //change tint to white
                binding.categoryImage.setColorFilter(
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