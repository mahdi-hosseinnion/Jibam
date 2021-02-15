package com.example.jibi.ui.main.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.jibi.R
import com.example.jibi.models.CategoryImages
import com.example.jibi.ui.main.transaction.TransactionListAdapter.Companion.listOfColor
import kotlinx.android.synthetic.main.layout_category_images_header.view.*
import kotlinx.android.synthetic.main.layout_category_images_list_item.view.*
import kotlin.collections.ArrayList


class AddCategoryListAdapter(
    private val requestManager: RequestManager?,
    private val interaction: Interaction? = null,
    private val packageName: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

//    private var headersList: MutableSet<String> = emptySet<String>() as MutableSet<String>
//    private var currentHeaderName: String? = null

    companion object {
        private const val TAG: String = "AppDebug"

        const val YESTERDAY = "Yesterday"
        const val TODAY = "Today"

        const val HEADER_ITEM = -3

        private const val IMAGE_ITEM = 0

        private val HEADER_MARKER = CategoryImages(
            HEADER_ITEM,
            "",
            "",
        )

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
                    interaction = interaction
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
                holder.bind(differ.currentList[position].group_name)
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

    fun getCategoryImages(position: Int): CategoryImages = differ.currentList[position]

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
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: CategoryImages) = with(itemView)
        {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            val categoryImageUrl = this.resources.getIdentifier(
                "ic_cat_${item.image_res}",
                "drawable",
                packageName
            )

            // set background
            if (item.id > 0) {
                itemView.category_image_card_view.setCardBackgroundColor(
                    resources.getColor(
                        listOfColor[(item.id.minus(
                            1
                        ))]
                    )
                )
            }
            //load image
            requestManager
                ?.load(categoryImageUrl)
                ?.centerInside()
                ?.transition(withCrossFade())
                ?.error(R.drawable.ic_error)
                ?.into(itemView.category_images)
        }


    }


    class HeaderViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(headerName: String) = with(itemView) {
            itemView.header_name.text = headerName
        }
    }


    interface Interaction {

        fun onItemSelected(position: Int, categoryImages: CategoryImages)

        fun restoreListPosition()
    }
}