package com.ssmmhh.jibam.presentation.categories.addcategoires

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.entity.CategoryImageEntity
import com.ssmmhh.jibam.databinding.LayoutCategoryImagesHeaderBinding
import com.ssmmhh.jibam.databinding.LayoutCategoryImagesListItemBinding

class AddCategoryListAdapter(
    private val viewModel: AddCategoryViewModel,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: List<AddCategoryRecyclerViewItem> = emptyList()

    /**
     * selected image id to change its background color.
     */
    private var selectedImageId: Int? = null

    /**
     * Stores selected image position so when the user changes the image we can use
     * notifyItemChanged() instead of notifyDataSetChanged() to notify the new image bind function
     * to update image's background.
     */
    private var selectedImagePosition: Int? = null

    class ImageViewHolder(
        private val binding: LayoutCategoryImagesListItemBinding,
        private val viewModel: AddCategoryViewModel,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryImageEntity, selectedImageId: Int?) = with(binding) {
            this.viewmodel = viewModel
            this.item = item
            this.position = adapterPosition

            if (item.id == selectedImageId) {
                setCategoryImageBackgroundColorTo(item.image_background_color)
                setCategoryImageTintColorTo(R.color.white)
            } else {
                setCategoryImageBackgroundColorToDefaultColor()
                setCategoryImageTintColorTo(R.color.black)
            }
        }

        private fun setCategoryImageBackgroundColorToDefaultColor() {
            //set to default (gray) background
            val cricle_background = ResourcesCompat.getDrawable(
                itemView.resources,
                R.drawable.shape_category_item_circle,
                null
            )
            binding.categoryImagesFrame.background = cricle_background
        }

        private fun setCategoryImageBackgroundColorTo(backgroundColor: String) {
            val drawable = binding.categoryImagesFrame.background as Drawable
            drawable.setColorFilter(
                Color.parseColor(backgroundColor), PorterDuff.Mode.SRC
            )
        }

        private fun setCategoryImageTintColorTo(@ColorRes color: Int) {
            binding.categoryImages.setColorFilter(
                itemView.resources.getColor(color),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    class HeaderViewHolder(
        val binding: LayoutCategoryImagesHeaderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(header: AddCategoryRecyclerViewItem.Header) = with(binding) {
            headerName.text = header.getCategoryGroupNameFromStringFile(itemView.context)
        }
    }

    fun submitList(
        images: List<AddCategoryRecyclerViewItem>
    ) {
        data = images
        notifyDataSetChanged()
    }

    fun setSelectedImageTo(id: Int, position: Int?) {
        if (selectedImageId == id) return
        val previousImageId = selectedImageId
        var previousImagePosition = selectedImagePosition
        selectedImageId = id
        selectedImagePosition = position
        //Notify last position to change background to default.
        if (previousImagePosition == null) {
            previousImagePosition = getPositionOfImageWithIdInList(previousImageId)
        }
        if (selectedImagePosition == null) {
            selectedImagePosition =  getPositionOfImageWithIdInList(id)
        }
        previousImagePosition?.let { notifyItemChanged(it) }
        selectedImagePosition?.let { notifyItemChanged(it) }

    }

    /**
     * Iterate through [data] to find position of image with [id].
     */
    private fun getPositionOfImageWithIdInList(id: Int?): Int? {
        if (id == null) return null
        data.forEachIndexed { index, item ->
            if (item is AddCategoryRecyclerViewItem.CategoryImage) {
                if (item.categoryImage.id == id) {
                    return index
                }
            }
        }
        return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            AddCategoryRecyclerViewItem.HEADER_VIEW_TYPE -> {
                return HeaderViewHolder(
                    binding = LayoutCategoryImagesHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                )
            }
            else -> {
                return ImageViewHolder(
                    binding = LayoutCategoryImagesListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    viewModel = viewModel,
                )
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        when (holder) {
            is ImageViewHolder -> {
                if (item is AddCategoryRecyclerViewItem.CategoryImage)
                    holder.bind(item.categoryImage, selectedImageId)
            }
            is HeaderViewHolder -> {
                if (item is AddCategoryRecyclerViewItem.Header)
                    holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = data[position].itemType

    override fun getItemCount(): Int = data.size

}