package com.ssmmhh.jibam.presentation.categories.addcategoires

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.LayoutCategoryImagesHeaderBinding
import com.ssmmhh.jibam.databinding.LayoutCategoryImagesListItemBinding
import com.ssmmhh.jibam.data.source.local.entity.CategoryImageEntity

class AddCategoryListAdapter(
    private val viewModel: AddCategoryViewModel,
    private val requestManager: RequestManager?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: List<AddCategoryRecyclerViewItem> = emptyList()

    private var currentSelectedItem: OnOthersSelectedListener? = null

    companion object {
        const val DEFAULT_CATEGORY_IMAGE_POSITION = 1

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
                    holder.bind(item.categoryImage)
            }
            is HeaderViewHolder -> {
                if (item is AddCategoryRecyclerViewItem.Header)
                    holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = data[position].itemType

    override fun getItemCount(): Int = data.size

    fun submitList(
        images: List<AddCategoryRecyclerViewItem>
    ) {
        data = images
        notifyDataSetChanged()
    }

    inner class ImageViewHolder
    constructor(
        val binding: LayoutCategoryImagesListItemBinding,
        private val viewModel: AddCategoryViewModel,
        val requestManager: RequestManager?,
    ) : RecyclerView.ViewHolder(binding.root), OnOthersSelectedListener {

        fun bind(item: CategoryImageEntity) = with(itemView)
        {
            restoreToDefaultBackground()

            if (adapterPosition == DEFAULT_CATEGORY_IMAGE_POSITION && currentSelectedItem == null) {
                //this viewHolder is the first image
                onClickedOnItem(item)
            }

            itemView.setOnClickListener {
                onClickedOnItem(item)
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

        private fun onClickedOnItem(item: CategoryImageEntity) {
            viewModel.setCategoryImage(item)
            //set last selected item to
            currentSelectedItem?.restoreToDefaultBackground()
            //set lister to new one
            currentSelectedItem = this@ImageViewHolder
            //change to new one
            currentSelectedItem?.setSelectedBackground(item.id, item.image_background_color)
        }

        override fun restoreToDefaultBackground() {

            //set to default (gray) background
            val cricle_background = ResourcesCompat.getDrawable(
                itemView.resources,
                R.drawable.shape_category_item_circle,
                null
            )
            binding.categoryImagesFrame.background = cricle_background

            //change image tint to black
            binding.categoryImages.setColorFilter(
                itemView.resources.getColor(R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        override fun setSelectedBackground(categoryId: Int, backgroundColor: String) {
            try {


                val circle_drawable = binding.categoryImagesFrame.background as Drawable

                circle_drawable?.setColorFilter(
                    Color.parseColor(backgroundColor), PorterDuff.Mode.MULTIPLY
                )


                //change tint to white
                binding.categoryImages.setColorFilter(
                    itemView.resources.getColor(R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } catch (e: Exception) {
                Log.e("CategoryViewHolder", "setSelectedBackground: message: ${e.message}", e)
            }
        }
    }

    class HeaderViewHolder
    constructor(
        val binding: LayoutCategoryImagesHeaderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(header: AddCategoryRecyclerViewItem.Header) = with(binding) {
            headerName.text = header.getCategoryGroupNameFromStringFile(itemView.context)
        }
    }

    interface OnOthersSelectedListener {

        //gray background(not selected)
        fun restoreToDefaultBackground()

        //color background(selected)
        fun setSelectedBackground(categoryId: Int, backgroundColor: String)

    }

}