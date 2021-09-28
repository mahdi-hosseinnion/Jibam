package com.example.jibi.ui.main.transaction.categories.addcategoires

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.jibi.R
import com.example.jibi.models.CategoryImages
import com.example.jibi.util.CategoriesImageBackgroundColors
import kotlinx.android.synthetic.main.layout_category_images_header.view.*
import kotlinx.android.synthetic.main.layout_category_images_list_item.view.*


class AddCategoryListAdapter(
    private val requestManager: RequestManager?,
    private val interaction: Interaction? = null,
    private val packageName: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

//    private var headersList: MutableSet<String> = emptySet<String>() as MutableSet<String>
//    private var currentHeaderName: String? = null

    private var currentSelectedItem: OnOthersSelectedListener? = null

    companion object {
        private const val TAG = "AddCategoryListAdapter"

        const val YESTERDAY = "Yesterday"
        const val TODAY = "Today"

        const val HEADER_ITEM = -3

        private const val IMAGE_ITEM = 0

        private val HEADER_MARKER = CategoryImages(
            HEADER_ITEM,
            "",
            "",
        )

        const val DEFAULT_CATEGORY_IMAGE_POSITION = 1

    }

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CategoryImages>() {

        override fun areItemsTheSame(oldItem: CategoryImages, newItem: CategoryImages): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryImages, newItem: CategoryImages): Boolean {
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
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_category_images_header,
                        parent,
                        false
                    ),
                    interaction = interaction,
                    packageName = packageName
                )
            }
            else -> {
                return ImageViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_category_images_list_item,
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager,
                    packageName = packageName
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

    fun getCategoryImages(position: Int): CategoryImages? = try {
        differ.currentList[position]
    } catch (e: Exception) {
        null
    }

    fun insertCategoryImagesAt(
        transaction: CategoryImages,
        position: Int?,
        header: CategoryImages?
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

    fun removeAt(position: Int): CategoryImages? {
        val newList = differ.currentList.toMutableList()
        val beforeCategoryImages = differ.currentList[position.minus(1)]
        val afterCategoryImages = differ.currentList[position.plus(1)]

        var removedHeader: CategoryImages? = null

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
        categoryImagesList: List<CategoryImages>?
    ) {

        val finalList = ArrayList<CategoryImages>()
        categoryImagesList?.sortedBy { it.group_name }?.let {
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
        itemView: View,
        val requestManager: RequestManager?,
        private val interaction: Interaction?,
        val packageName: String
    ) : RecyclerView.ViewHolder(itemView), OnOthersSelectedListener {

        fun bind(item: CategoryImages) = with(itemView)
        {
            restoreToDefaultBackground()

            if (adapterPosition == DEFAULT_CATEGORY_IMAGE_POSITION && currentSelectedItem == null) {
                //this viewHolder is the first image
                onClickedOnItem(item)
            }

            itemView.setOnClickListener {
                onClickedOnItem(item)
            }

            val categoryImageUrl = this.resources.getIdentifier(
                "ic_cat_${item.image_res}",
                "drawable",
                packageName
            )

            //load image
            requestManager
                ?.load(categoryImageUrl)
                ?.centerInside()
                ?.transition(withCrossFade())
                ?.error(R.drawable.ic_error)
                ?.into(itemView.category_images)
        }

        private fun onClickedOnItem(item: CategoryImages) {
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
            itemView.category_images_frame.background = cricle_background

            //change image tint to black
            itemView.category_images.setColorFilter(
                itemView.resources.getColor(R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        override fun setSelectedBackground(categoryId: Int) {
            try {


                val circle_drawable = itemView.category_images_frame.background as Drawable

                circle_drawable?.setColorFilter(
                    itemView.resources.getColor(
                        CategoriesImageBackgroundColors.getCategoryColorById(categoryId)
                    ), PorterDuff.Mode.MULTIPLY
                )


                //change tint to white
                itemView.category_images.setColorFilter(
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
        itemView: View,
        private val interaction: Interaction?,
        private val packageName: String
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(headerName: CategoryImages) = with(itemView) {
            itemView.header_name.text = headerName.getCategoryGroupNameFromStringFile(
                resources,
                packageName = packageName
            ) {
                it.group_name
            }
        }
    }


    interface Interaction {

        fun onItemSelected(position: Int, categoryImages: CategoryImages)

        fun restoreListPosition()
    }

    interface OnOthersSelectedListener {

        //gray background(not selected)
        fun restoreToDefaultBackground()

        //color background(selected)
        fun setSelectedBackground(categoryId: Int)

    }

}