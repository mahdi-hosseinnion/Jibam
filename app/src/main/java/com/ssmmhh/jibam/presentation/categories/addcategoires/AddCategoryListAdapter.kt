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

    private var currentSelectedItem: OnOthersSelectedListener? = null

    companion object {
        private const val TAG = "AddCategoryListAdapter"

        const val HEADER_ITEM = -3

        private const val IMAGE_ITEM = 0

        private val HEADER_MARKER = CategoryImageEntity(
            HEADER_ITEM,
            "",
            "",
            ""
        )

        const val DEFAULT_CATEGORY_IMAGE_POSITION = 1

    }

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CategoryImageEntity>() {

        override fun areItemsTheSame(
            oldItem: CategoryImageEntity,
            newItem: CategoryImageEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: CategoryImageEntity,
            newItem: CategoryImageEntity
        ): Boolean {
            return oldItem == newItem
        }

    }
    private val differ =
        AsyncListDiffer(
            CategoryImagesRecyclerChangeCallback(this),
            AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
        )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {

            HEADER_ITEM -> {
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

    internal inner class CategoryImagesRecyclerChangeCallback(
        private val adapter: AddCategoryListAdapter
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
            is ImageViewHolder -> {
                holder.bind(differ.currentList[position])
            }
            is HeaderViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].id > -1) {
            return IMAGE_ITEM
        }
        return differ.currentList[position].id
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun submitList(
        categoryImageEntityList: List<CategoryImageEntity>?
    ) {
        //TODO rewrite this using group by
        val finalList = ArrayList<CategoryImageEntity>()
        categoryImageEntityList?.sortedBy { it.groupName }?.let {
            var tempName = ""
            //add category with maker for showing
            for (item in it) {
                if (item.groupName == tempName) {
                    finalList.add(item)
                } else {
                    //add header
                    tempName = item.groupName
                    finalList.add(HEADER_MARKER.copy(groupName = tempName))
                    finalList.add(item)
                }
            }
        }
        differ.submitList(finalList)
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

        fun bind(headerName: CategoryImageEntity) = with(itemView) {
            binding.headerName.text = headerName.getCategoryGroupNameFromStringFile(context)
        }
    }

    interface OnOthersSelectedListener {

        //gray background(not selected)
        fun restoreToDefaultBackground()

        //color background(selected)
        fun setSelectedBackground(categoryId: Int, backgroundColor: String)

    }

}