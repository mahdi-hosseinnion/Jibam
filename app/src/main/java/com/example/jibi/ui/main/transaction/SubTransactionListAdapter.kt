package com.example.jibi.ui.main.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.models.Record
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


class SubTransactionListAdapter(
    private val requestManager: RequestManager?,
    private val interaction: Interaction? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TAG: String = "AppDebug"
        private const val NO_MORE_RESULTS = -1
        private const val BLOG_ITEM = 0

    }

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


    internal inner class BlogRecyclerChangeCallback(
        private val adapter: SubTransactionListAdapter
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
                holder.bind(differ.currentList[position])
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].id > -1) {
            return BLOG_ITEM
        }
        return differ.currentList[position].id
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
        blogList: List<Record>?
    ) {
        val newList = blogList?.toMutableList()

        differ.submitList(newList)
    }

    inner class TransViewHolder
    constructor(
        itemView: View,
        val requestManager: RequestManager?,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Record) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }
//            if (itemCount == adapterPosition.plus(1)) {
//                itemView.transaction_divider.visibility = View.GONE
//            } else {
//                itemView.transaction_divider.visibility = View.VISIBLE
//            }
            if (item.memo.isNullOrBlank()) {
                itemView.main_text.text = "UNKNOWN CATEGORY WITH ID: ${item.cat_id.toString()}"
            } else {
                itemView.main_text.text = item.memo
            }

            itemView.price.text = "${separate3By3(item.money)}"
            if (item.money >= 0) {
                itemView.price.setTextColor(resources.getColor(R.color.incomeTextColor))
                itemView.priceCard.setCardBackgroundColor(resources.getColor(R.color.incomeColor))
            } else {
                itemView.price.setTextColor(resources.getColor(R.color.expensesTextColor))
                itemView.priceCard.setCardBackgroundColor(resources.getColor(R.color.expensesColor))
            }

            //TODO
//            itemView.card
//           //            requestManager
////                .load(item.image)
////                .transition(withCrossFade())
////                .into(itemView.category_iamge)
        }
        private fun separate3By3(money1: Double):String{
            var money = money1
            if (money<0.0){
                money*=-1.0
            }
            val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
            formatter.applyPattern("#,###,###,###.###")
            return formatter.format(money)
        }
    }


    interface Interaction {

        fun onItemSelected(position: Int, item: Record)

    }
}