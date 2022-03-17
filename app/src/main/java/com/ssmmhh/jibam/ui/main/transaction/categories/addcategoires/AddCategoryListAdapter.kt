package com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires

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
import com.ssmmhh.jibam.persistence.entities.CategoryImageEntity
import com.ssmmhh.jibam.util.CategoriesImageBackgroundColors


class AddCategoryListAdapter(
    private val requestManager: RequestManager?,
    private val interaction: Interaction? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var currentSelectedItem: OnOthersSelectedListener? = null

    companion object {
        private const val TAG = "AddCategoryListAdapter"

        const val YESTERDAY = "Yesterday"
        const val TODAY = "Today"

        const val HEADER_ITEM = -3

        private const val IMAGE_ITEM = 0

        private val HEADER_MARKER = CategoryImageEntity(
            HEADER_ITEM,
            "",
            "",
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
                    LayoutCategoryImagesHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    interaction = interaction,
                )
            }
            else -> {
                return ImageViewHolder(
                    LayoutCategoryImagesListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    interaction = interaction,
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

    private fun isHeader(position: Int): Boolean {
        if (position <= itemCount) {
            return differ.currentList[position].id < 0
        }
        return true
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].id > -1) {
            return IMAGE_ITEM
        }
        return differ.currentList[position].id
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun getCategoryImages(position: Int): CategoryImageEntity? = try {
        differ.currentList[position]
    } catch (e: Exception) {
        null
    }

    fun insertCategoryImagesAt(
        transaction: CategoryImageEntity,
        position: Int?,
        header: CategoryImageEntity?
    ) {
        val newList = differ.currentList.toMutableList()
        if (position != null) {
            if (header != null) {
                newList.add(position.minus(1), header)
            }
            newList.add(position, transaction)
        } else {
            if (header != null) {
                newList.add(header)
            }
            newList.add(transaction)
        }
        differ.submitList(newList)
    }

    fun removeAt(position: Int): CategoryImageEntity? {
        val newList = differ.currentList.toMutableList()
        val beforeCategoryImages = differ.currentList[position.minus(1)]
        val afterCategoryImages = differ.currentList[position.plus(1)]

        var removedHeader: CategoryImageEntity? = null

        if (beforeCategoryImages.id == HEADER_ITEM &&
            afterCategoryImages.id == HEADER_ITEM
        ) {
            removedHeader = newList.removeAt(position.minus(1))
        }
        newList.removeAt(position)
        differ.submitList(newList)
        return removedHeader
    }
//    // Prepare the images that will be displayed in the RecyclerView.
//    // This also ensures if the network connection is lost, they will be in the cache
//    fun preloadGlideImages(
//        requestManager: RequestManager,
//        list: List<CategoryImages>
//    ){
//        for(CategoryImages in list){
//            requestManager
//                .load(CategoryImages.image)
//                .preload()
//        }
//    }

    fun submitList(
        categoryImageEntityList: List<CategoryImageEntity>?
    ) {

        val finalList = ArrayList<CategoryImageEntity>()
        categoryImageEntityList?.sortedBy { it.group_name }?.let {
            var tempName = ""
            //add category with maker for showing
            for (item in it) {
                if (item.group_name == tempName) {
                    finalList.add(item)
                } else {
                    //add header
                    tempName = item.group_name
                    finalList.add(HEADER_MARKER.copy(group_name = tempName))
                    finalList.add(item)
                }
            }
        }
        differ.submitList(finalList)
    }

    inner class ImageViewHolder
    constructor(
        val binding: LayoutCategoryImagesListItemBinding,
        val requestManager: RequestManager?,
        private val interaction: Interaction?,
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
            Log.d(TAG, "onClickedOnItem: $item ")
            interaction?.onItemSelected(adapterPosition, item)
            //set last selected item to
            currentSelectedItem?.restoreToDefaultBackground()
            //set lister to new one
            currentSelectedItem = this@ImageViewHolder
            //change to new one
            currentSelectedItem?.setSelectedBackground(item.id)
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

        override fun setSelectedBackground(categoryId: Int) {
            try {


                val circle_drawable = binding.categoryImagesFrame.background as Drawable

                circle_drawable?.setColorFilter(
                    itemView.resources.getColor(
                        CategoriesImageBackgroundColors.getCategoryColorById(categoryId)
                    ), PorterDuff.Mode.MULTIPLY
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
        private val interaction: Interaction?,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(headerName: CategoryImageEntity) = with(itemView) {
            binding.headerName.text = headerName.getCategoryGroupNameFromStringFile(context)
        }
    }


    interface Interaction {

        fun onItemSelected(position: Int, categoryImageEntity: CategoryImageEntity)

        fun restoreListPosition()
    }

    interface OnOthersSelectedListener {

        //gray background(not selected)
        fun restoreToDefaultBackground()

        //color background(selected)
        fun setSelectedBackground(categoryId: Int)

    }

}