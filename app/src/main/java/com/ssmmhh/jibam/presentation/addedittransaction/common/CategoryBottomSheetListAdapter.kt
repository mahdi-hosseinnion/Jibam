package com.ssmmhh.jibam.presentation.addedittransaction.common

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.LayoutCategoryListItemBinding
import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.util.EspressoIdlingResources

//TODO ("Remove diffUtil from this adapter b/c it does not change too often")
class CategoryBottomSheetListAdapter(
    private val requestManager: RequestManager,
    private val interaction: Interaction? = null,
    private var selectedItemId: Int?,
    private val onItemSelected: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG: String = "CategoryBottomSheetListAdapter"
    private val BLOG_ITEM = 0

    private var selectedItemPosition: Int? = null

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
            onItemSelected = { id, position ->
                onNewItemSelected(id, position)
                onItemSelected()
            }
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

    /**
     * Set previously selected category background to default.
     */
    private fun onNewItemSelected(id: Int, position: Int) {
        selectedItemId = id
        selectedItemPosition?.let { notifyItemChanged(it) }
        selectedItemPosition = position
        notifyItemChanged(position)
    }

    /**
     * Set selected category background to default.
     */
    fun deselectSelectedItem() {
        if (selectedItemId == null) return
        selectedItemId = null
        selectedItemPosition?.let { notifyItemChanged(it) } ?: notifyDataSetChanged()
        selectedItemPosition = null
    }

    class CategoryViewHolder
    constructor(
        val binding: LayoutCategoryListItemBinding,
        val requestManager: RequestManager,
        private val interaction: Interaction?,
        private val onItemSelected: (id: Int, position: Int) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Category, selectedItemId: Int?) = with(itemView) {
            setItemBackgroundToDefaultColor()
            itemView.setOnClickListener {

                setItemBackgroundTo(item.image.backgroundColor, R.color.white)
                onItemSelected(item.id, adapterPosition)
                interaction?.onItemSelected(adapterPosition, item)
            }
            if (item.id == selectedItemId && item.id > 0) {
                setItemBackgroundTo(item.image.backgroundColor, R.color.white)
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

        private fun setItemBackgroundToDefaultColor() {
            setItemBackgroundTo(
                itemView.resources.getColor(
                    R.color.category_list_item_image_background_color
                ), R.color.black
            )
        }

        private fun setItemBackgroundTo(hexColor: String, imageTintColorId: Int) {
            setItemBackgroundTo(Color.parseColor(hexColor), imageTintColorId)
        }

        private fun setItemBackgroundTo(backgroundColor: Int, imageViewTintColorId: Int) {
            //Change background color
            binding.categoryImageFrame.background?.apply {
                setColorFilter(backgroundColor, PorterDuff.Mode.SRC)
            }

            //Change the image view tint color
            binding.categoryImage.setColorFilter(
                itemView.resources.getColor(imageViewTintColorId),
                PorterDuff.Mode.SRC_IN
            )
        }


    }

    interface Interaction {

        fun onItemSelected(position: Int, item: Category)
    }
}