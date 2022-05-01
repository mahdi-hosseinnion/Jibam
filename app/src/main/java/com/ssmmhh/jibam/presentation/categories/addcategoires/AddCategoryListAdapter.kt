package com.ssmmhh.jibam.presentation.categories.addcategoires

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.entity.CategoryImageEntity
import com.ssmmhh.jibam.databinding.LayoutCategoryImagesHeaderBinding
import com.ssmmhh.jibam.databinding.LayoutCategoryImagesListItemBinding

class AddCategoryListAdapter(
    private val viewModel: AddCategoryViewModel,
    private val requestManager: RequestManager?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: List<AddCategoryRecyclerViewItem> = emptyList()

    private var currentlySelectedImageId: Int? = null
    private var currentlySelectedImagePosition: Int? = null

    class ImageViewHolder(
        private val binding: LayoutCategoryImagesListItemBinding,
        private val viewModel: AddCategoryViewModel,
        private val requestManager: RequestManager?,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryImageEntity, currentlySelectedImageId: Int?) = with(itemView) {
            itemView.setOnClickListener {
                viewModel.setCategoryImage(item, adapterPosition)
            }
            if (item.id == currentlySelectedImageId) {
                setCategoryImageBackgroundColorTo(item.image_background_color)
                setCategoryImageTintColorTo(R.color.white)
            } else {
                setCategoryImageBackgroundColorToDefaultColor()
                setCategoryImageTintColorTo(R.color.black)
            }
            val categoryImageResourceId = item.getCategoryImageResourceId(context)
            //load image
            requestManager
                ?.load(categoryImageResourceId)
                ?.centerInside()
                ?.transition(withCrossFade())
                ?.error(R.drawable.ic_error)
                ?.into(binding.categoryImages)
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

    fun setCurrentlySelectedImageTo(id: Int, position: Int?) {
        val previousImagePosition = currentlySelectedImagePosition
        currentlySelectedImageId = id
        currentlySelectedImagePosition = position
        //Notify last position to change background to default.
        if (position == null || previousImagePosition == null) {
            notifyDataSetChanged()
            return
        }
        notifyItemChanged(previousImagePosition)
        notifyItemChanged(position)

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
                    requestManager = requestManager,
                )
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        when (holder) {
            is ImageViewHolder -> {
                if (item is AddCategoryRecyclerViewItem.CategoryImage)
                    holder.bind(item.categoryImage, currentlySelectedImageId)
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