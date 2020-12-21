package com.example.jibi.ui.main.transaction

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.models.Record
import com.example.jibi.util.GenericViewHolder
import kotlinx.android.synthetic.main.fragment_add_transaction.view.*
import kotlinx.android.synthetic.main.layout_transacion_header.view.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.*

class TransactionListAdapter(
    private val requestManager: RequestManager?,
    private val interaction: Interaction? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG: String = "AppDebug"
    private val NO_MORE_RESULTS = -1
    private val BLOG_ITEM = 0
    private val NO_MORE_RESULTS_BLOG_MARKER = Record(
        NO_MORE_RESULTS,
        0,
        "",
        0,
        0
    )

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Record>() {

        override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean {
            return oldItem == newItem
        }

    }
    private val differ =
        AsyncListDiffer(
            BlogRecyclerChangeCallback(this),
            AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
        )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {


            NO_MORE_RESULTS -> {
                Log.e(TAG, "onCreateViewHolder: No more results...")
                return GenericViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_no_more_results,
                        parent,
                        false
                    )
                )
            }
            BLOG_ITEM -> {
                return TransViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_transaction_list_item,
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
            else -> {
                return TransViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_transaction_list_item,
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
        }
    }

    internal inner class BlogRecyclerChangeCallback(
        private val adapter: TransactionListAdapter
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
            is TransViewHolder -> {
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
//        list: List<Record>
//    ){
//        for(Record in list){
//            requestManager
//                .load(Record.image)
//                .preload()
//        }
//    }

    fun submitList(
        blogList: List<Record>?,
        isQueryExhausted: Boolean
    ) {
        val newList = blogList?.toMutableList()
        if (isQueryExhausted)
            newList?.add(NO_MORE_RESULTS_BLOG_MARKER)
        val commitCallback = Runnable {
            // if process died must restore list position
            // very annoying
            interaction?.restoreListPosition()
        }
        differ.submitList(newList, commitCallback)
    }

    class TransViewHolder
    constructor(
        itemView: View,
        val requestManager: RequestManager?,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Record) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }


            if (item.memo.isNullOrBlank()) {
                itemView.main_text.text = "UNKNOWN CATEGORY WITH ID: ${item.cat_id.toString()}"
            } else {
                itemView.main_text.text = item.memo
            }
            if(item.money>=0){
                itemView.price.text = "+${item.money}"
                itemView.price.setTextColor(Color.GREEN)
            }else{
                itemView.price.text = "-${item.money}"
                itemView.price.setTextColor(Color.RED)
            }
            //TODO
//            itemView.card
//           //            requestManager
////                .load(item.image)
////                .transition(withCrossFade())
////                .into(itemView.category_iamge)
        }
    }

    class HeaderViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Record) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }
            //money for expenses
            if (item.money!=0){
                itemView.header_expenses_sum.text="Expenses: ${item.money}"
            }else{
                itemView.header_expenses_sum.text=""

            }
            //cat_id for income
            if (item.cat_id!=0){
                itemView.header_expenses_sum.text="Income: ${item.cat_id}"
            }else{
                itemView.header_expenses_sum.text=""

            }
//            itemView.header_date.text = DateUtils.convertLongToStringDate(item.date)
        }
    }

    interface Interaction {

        fun onItemSelected(position: Int, item: Record)

        fun restoreListPosition()
    }
}