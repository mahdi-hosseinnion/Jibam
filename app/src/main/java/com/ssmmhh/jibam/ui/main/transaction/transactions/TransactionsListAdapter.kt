package com.ssmmhh.jibam.ui.main.transaction.transactions

import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.LayoutTransacionHeaderBinding
import com.ssmmhh.jibam.databinding.LayoutTransactionListItemBinding
import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.models.Transaction
import com.ssmmhh.jibam.util.CategoriesImageBackgroundColors
import com.ssmmhh.jibam.util.GenericViewHolder
import com.ssmmhh.jibam.util.separate3By3
import java.util.*
import kotlin.collections.ArrayList


class TransactionsListAdapter(
    private val requestManager: RequestManager?,
    private val interaction: Interaction? = null,
    private val packageName: String,
    private val currentLocale: Locale

) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var categoryList = ArrayList<Category>()

    companion object {
        private const val TAG: String = "AppDebug"
        const val NO_MORE_RESULTS = -1

        const val NO_RESULT_FOUND = -2

        const val DATABASE_IS_EMPTY = -4

        //MAKE THIS PERSIAN
        const val YESTERDAY = "Yesterday"
        const val TODAY = "Today"

        const val HEADER_ITEM = -3
        private const val TRANSACTION_ITEM = 0
        private val NO_MORE_RESULTS_BLOG_MARKER = Transaction(
            NO_MORE_RESULTS,
            0.0,
            "NO_MORE_RESULTS_BLOG_MARKER",
            0.0
        )
        val NO_RESULT_FOUND_FOR_THIS_QUERY_MARKER = Transaction(
            NO_RESULT_FOUND,
            0.0,
            "NO_RESULT_FOUND_FOR_THIS_QUERY_MARKER",
            0.0,
        )
        val NO_RESULT_FOUND_IN_DATABASE_OLD = Transaction(
            DATABASE_IS_EMPTY,
            0.0,
            "NO_RESULT_FOUND_IN_DATABASE",
            0.0

        )
        val NO_RESULT_FOUND_IN_DATABASE = Transaction(
            DATABASE_IS_EMPTY,
            0.0,
            "NO_RESULT_FOUND_IN_DATABASE",
            0.0
        )

        //list of supported patter
        //https://stackoverflow.com/a/12781297/10362460
//        "E MM/dd/yy",
        //"^" is just marker
        const val DAY_OF_WEEK_MARKER = '^'
        const val HEADER_DATE_PATTERN = "E,$DAY_OF_WEEK_MARKER MMM dd yyyy"
//        const val HEADER_DATE_PATTERN="MM/dd/yy (E)"

    }

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Transaction>() {

        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
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
                    ),
                    R.id.big_JIBI,
                    R.string.jibi_capital
                )
            }
            NO_RESULT_FOUND -> {
                Log.e(TAG, "onCreateViewHolder: NO result  found with this query or filter ...")
                return GenericViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_no_results_found_list_item,
                        parent,
                        false
                    ),
                    R.id.no_result_found,
                    R.string.no_result_found_for_this_search
                )
            }
            DATABASE_IS_EMPTY -> {
                Log.e(TAG, "onCreateViewHolder: Database is empty...")
                return GenericViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_no_results_found_in_database_list_item,
                        parent,
                        false
                    ),
                    R.id.nullTRANSACTION,
                    R.string.insert_some_transaction_with_add_button
                )
            }
            TRANSACTION_ITEM -> {
                return TransViewHolder(
                    binding = LayoutTransactionListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager,
                    packageName = packageName
                )
            }
            HEADER_ITEM -> {
                return HeaderViewHolder(
                    binding = LayoutTransacionHeaderBinding    .inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    interaction = interaction,
                    currentLocale = currentLocale
                )
            }
            else -> {
                return TransViewHolder(
                    binding = LayoutTransactionListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager,
                    packageName = packageName
                )
            }
//            else -> {
//                return TransCardViewHolder(
//                    LayoutInflater.from(parent.context).inflate(
//                        R.layout.layout_new_transaction_list_item,
//                        parent,
//                        false
//                    ),
//                    interaction = interaction,
//                    requestManager = requestManager
//                )
//            }
        }
    }

    internal inner class BlogRecyclerChangeCallback(
        private val adapter: TransactionsListAdapter
    ) : ListUpdateCallback {

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position, count, payload)
        }

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyDataSetChanged()
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
                holder.bind(differ.currentList[position], isHeader(position.plus(1)))
            }
            is HeaderViewHolder -> {
                holder.bind(differ.currentList[position])
            }
//            is TransCardViewHolder -> {
//                val currentList = differ.currentList
//                val header = headerList?.get(position)
//                var headerPositionInMainList: Int? = null
//                for (i in currentList.indices) {
//                    if (currentList[i] == header) {
//                        headerPositionInMainList = i
//                    }
//                }
//                if (headerPositionInMainList == null) {
//                    Log.e(TAG, "onBindViewHolder: CANNOT FIND THE HEADER")
//                    return
//                }
//                if (header == null) {
//                    Log.e(TAG, "onBindViewHolder: CANNOT FIND THE HEADER HEADER IS NULL")
//                    return
//                }
//                val transactionList = differ.currentList.subList(
//                    headerPositionInMainList,
//                    headerPositionInMainList + header.date
//                )
//                holder.bind(header, transactionList)
//            }
        }
    }

    private fun isHeader(position: Int): Boolean {
        if (position < itemCount) {
            return differ.currentList[position].id < 0
        }
        return true
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].id > -1) {
            return TRANSACTION_ITEM
        }
        return differ.currentList[position].id
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun getTransaction(position: Int): Transaction = differ.currentList[position]

    fun insertTransactionAt(transaction: Transaction, position: Int?, header: Transaction?) {
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

    fun removeAt(position: Int): Transaction? {
        val newList = differ.currentList.toMutableList()
        val beforeTransaction = try {
            differ.currentList[position.minus(1)]
        } catch (e: Exception) {
            null
        }
        val afterTransaction = try {
            differ.currentList[position.plus(1)]
        } catch (e: Exception) {
            null
        }

        var removedHeader: Transaction? = null

        if (beforeTransaction?.id == HEADER_ITEM &&
            afterTransaction?.id == HEADER_ITEM
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
//        list: List<Transaction>
//    ){
//        for(Transaction in list){
//            requestManager
//                .load(Transaction.image)
//                .preload()
//        }
//    }

    fun submitList(
        transList: List<Transaction>?,
        isQueryExhausted: Boolean
    ) {
        val newList = transList?.toMutableList()
        if (isQueryExhausted &&
            newList?.get(0) != NO_RESULT_FOUND_FOR_THIS_QUERY_MARKER &&
            newList?.get(0) != NO_RESULT_FOUND_IN_DATABASE
        ) {
            if ((newList?.size ?: 0) > 10) {
                newList?.add(NO_MORE_RESULTS_BLOG_MARKER)
            }
        }
        val commitCallback = Runnable {
            // if process died must restore list position
            // very annoying
            interaction?.restoreListPosition()
        }
        differ.submitList(newList, commitCallback)
    }

    inner class TransViewHolder
    constructor(
        val binding: LayoutTransactionListItemBinding,
        val requestManager: RequestManager?,
        private val interaction: Interaction?,
        val packageName: String
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Transaction, isNextItemHeader: Boolean = false) = with(itemView) {

            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            if (isNextItemHeader) {
                binding.transactionDivider.visibility = View.GONE
//                itemView.root_transaction_item.setBackgroundResource(R.drawable.tranaction_bottom_header_bg)
            } else {
                binding.transactionDivider.visibility = View.VISIBLE
//                itemView.root_transaction_item.setBackgroundResource(R.color.backGround_white)
            }

            if (item.memo.isNullOrBlank()) {
                binding.mainText.text = item.getCategoryNameFromStringFile(
                    this.resources,
                    this@TransViewHolder.packageName
                ) {
                    it.categoryName
                }
            } else {
                binding.mainText.text = item.memo
            }
            if (item.money >= 0.0) {
                //income
                binding.price.text = separate3By3(item.money, currentLocale)
                binding.price.setTextColor(resources.getColor(R.color.blue_500))
            } else {
                //expenses
                binding.price.text = separate3By3(item.money, currentLocale)
                binding.price.setTextColor(resources.getColor(R.color.red_500))
            }
            val categoryImageUrl = this.resources.getIdentifier(
                "ic_cat_${item.categoryImage}",
                "drawable",
                packageName
            )
            //TODO
//            itemView.card
            binding.cardView.setCardBackgroundColor(
                resources.getColor(
                    CategoriesImageBackgroundColors.getCategoryColorById(item.categoryId)
                )
            )

            requestManager
                ?.load(categoryImageUrl)
                ?.centerInside()
                ?.transition(withCrossFade())
                ?.error(R.drawable.ic_error)
                ?.into(binding.categoryImage)
        }

    }


    class HeaderViewHolder
    constructor(
        val binding: LayoutTransacionHeaderBinding,
        private val interaction: Interaction?,
        private val currentLocale: Locale
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Transaction) = with(itemView) {

            //hide margin for first object
//            if (adapterPosition<2){
//                val params = ((itemView.root_transaction_header).layoutParams) as RecyclerView.LayoutParams
//                params.setMargins(convertDpToPx(8), 0, convertDpToPx(8), 0) //substitute parameters for left, top, right, bottom
//
//                itemView.root_transaction_header.layoutParams = params
//            }
            //money for expenses
            Log.d(TAG, "bind: sum of all expenses = ${item.money}")
            if (item.money != 0.0) {
                binding.headerExpensesSum.text = "${resources.getString(R.string.expenses)}: ${
                    separate3By3(
                        item.money,
                        currentLocale
                    )
                }"
            } else {
                binding.headerExpensesSum.text = ""

            }
            //cat_id for income
            if (item.incomeSum != null && item.incomeSum != 0.0) {
                binding.headerIncomeSum.text = "${resources.getString(R.string.income)}: ${
                    separate3By3(
                        item.incomeSum,
                        currentLocale
                    )
                }"
            } else {
                binding.headerIncomeSum.text = ""
            }
            binding.headerDate.text = ""

            if (item.memo == TODAY) {
                binding.headerDateName.text = resources.getString(R.string.today)
            } else if (item.memo == YESTERDAY) {
                binding.headerDateName.text = resources.getString(R.string.yesterday)
            } else {
                try {
                    binding.headerDateName.text = item.memo?.substring(
                        0, item.memo.indexOf(
                            DAY_OF_WEEK_MARKER
                        )
                    )
                    binding.headerDate.text =
                        item.memo?.substring(item.memo.indexOf(DAY_OF_WEEK_MARKER).plus(1))
                } catch (e: Exception) {
                    binding.headerDate.text = "${item.memo}"
                }
            }
        }

        private fun convertDpToPx(dp: Int): Int {
            val r: Resources = itemView.resources
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                r.displayMetrics
            ).toInt()
        }
    }


    interface Interaction {

        fun onItemSelected(position: Int, item: Transaction)

        fun restoreListPosition()
    }
}