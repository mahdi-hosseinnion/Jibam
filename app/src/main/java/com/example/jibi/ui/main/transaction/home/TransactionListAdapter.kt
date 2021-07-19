package com.example.jibi.ui.main.transaction.home

import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.util.GenericViewHolder
import com.example.jibi.util.separate3By3
import kotlinx.android.synthetic.main.fragment_add_transaction.view.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.fragment_transaction.view.*
import kotlinx.android.synthetic.main.layout_new_transaction_list_item.view.*
import kotlinx.android.synthetic.main.layout_transacion_header.view.*
import kotlinx.android.synthetic.main.layout_transacion_header.view.header_date
import kotlinx.android.synthetic.main.layout_transacion_header.view.header_expenses_sum
import kotlinx.android.synthetic.main.layout_transacion_header.view.header_income_sum
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


abstract class TransactionListAdapter(
    private val requestManager: RequestManager?,
    private val interaction: Interaction? = null,
    private val packageName: String,
    private val currentLocale: Locale,
    private val _resources: Resources

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
        private val NO_MORE_RESULTS_BLOG_MARKER = Record(
            NO_MORE_RESULTS,
            0.0,
            "NO_MORE_RESULTS_BLOG_MARKER",
            0,
            0
        )
        val NO_RESULT_FOUND_FOR_THIS_QUERY_MARKER = Record(
            NO_RESULT_FOUND,
            0.0,
            "NO_RESULT_FOUND_FOR_THIS_QUERY_MARKER",
            0,
            0
        )
        val NO_RESULT_FOUND_IN_DATABASE = Record(
            DATABASE_IS_EMPTY,
            0.0,
            "NO_RESULT_FOUND_IN_DATABASE",
            0,
            0
        )

        //list of supported patter
        //https://stackoverflow.com/a/12781297/10362460
//        "E MM/dd/yy",
        //"^" is just marker
        const val DAY_OF_WEEK_MARKER = '^'
        const val HEADER_DATE_PATTERN = "E,$DAY_OF_WEEK_MARKER MMM dd yyyy"
//        const val HEADER_DATE_PATTERN="MM/dd/yy (E)"


        val listOfColor = listOf(
            R.color.category_background_color_1,
            R.color.category_background_color_2,
            R.color.category_background_color_3,
            R.color.category_background_color_4,
            R.color.category_background_color_5,
            R.color.category_background_color_6,
            R.color.category_background_color_7,
            R.color.category_background_color_8,
            R.color.category_background_color_9,
            R.color.category_background_color_10,
            R.color.category_background_color_11,
            R.color.category_background_color_12,
            R.color.category_background_color_13,
            R.color.category_background_color_14,
            R.color.category_background_color_15,
            R.color.category_background_color_16,
            R.color.category_background_color_17,
            R.color.category_background_color_18,
            R.color.category_background_color_19,
            R.color.category_background_color_20,
            R.color.category_background_color_21,
            R.color.category_background_color_22,
            R.color.category_background_color_23,
            R.color.category_background_color_24,
            R.color.category_background_color_25,
            R.color.category_background_color_26,
            R.color.category_background_color_27,
            R.color.category_background_color_28,
            R.color.category_background_color_29,
            R.color.category_background_color_30,
            R.color.category_background_color_31,
            R.color.category_background_color_32,
            R.color.category_background_color_33,
            R.color.category_background_color_34,
            R.color.category_background_color_35,
            R.color.category_background_color_36,
            R.color.category_background_color_37,
            R.color.category_background_color_38,
            R.color.category_background_color_39,
            R.color.category_background_color_40,
            R.color.category_background_color_41
        )
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

        when (viewType) {


            NO_MORE_RESULTS -> {
                Log.e(TAG, "onCreateViewHolder: No more results...")
                return GenericViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_no_more_results,
                        parent,
                        false
                    ),
                    _resources,
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
                    _resources,
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
                    _resources,
                    R.id.nullTRANSACTION,
                    R.string.insert_some_transaction_with_add_button
                )
            }
            TRANSACTION_ITEM -> {
                return TransViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_transaction_list_item,
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
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_transacion_header,
                        parent,
                        false
                    ),
                    interaction = interaction,
                    currentLocale = currentLocale, _resources
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
        private val adapter: TransactionListAdapter
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

    fun getRecord(position: Int): Record = differ.currentList[position]

    fun insertRecordAt(transaction: Record, position: Int?, header: Record?) {
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

    fun removeAt(position: Int): Record? {
        val newList = differ.currentList.toMutableList()
        val beforeRecord = differ.currentList[position.minus(1)]
        val afterRecord = differ.currentList[position.plus(1)]

        var removedHeader: Record? = null

        if (beforeRecord.id == HEADER_ITEM &&
            afterRecord.id == HEADER_ITEM
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
//        list: List<Record>
//    ){
//        for(Record in list){
//            requestManager
//                .load(Record.image)
//                .preload()
//        }
//    }

    fun submitList(
        transList: List<Record>?,
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
        itemView: View,
        val requestManager: RequestManager?,
        private val interaction: Interaction?,
        val packageName: String
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Record, isNextItemHeader: Boolean = false) = with(itemView) {
            var category = getCategoryById(item.cat_id)

            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            if (isNextItemHeader) {
                itemView.transaction_divider.visibility = View.GONE
//                itemView.root_transaction_item.setBackgroundResource(R.drawable.tranaction_bottom_header_bg)
            } else {
                itemView.transaction_divider.visibility = View.VISIBLE
//                itemView.root_transaction_item.setBackgroundResource(R.color.backGround_white)
            }

            if (item.memo.isNullOrBlank()) {
                itemView.main_text.text = category.getCategoryNameFromStringFile(
                    _resources,
                    this@TransViewHolder.packageName
                ) {
                    it.name
                }
            } else {
                itemView.main_text.text = item.memo
            }
            if (item.money >= 0.0) {
                itemView.price.text = separate3By3(item.money, currentLocale)
                itemView.price.setTextColor(resources.getColor(R.color.incomeTextColor))
                itemView.priceCard.setCardBackgroundColor(resources.getColor(R.color.incomeColor))
            } else {
                itemView.price.text = separate3By3(item.money, currentLocale)
                itemView.price.setTextColor(resources.getColor(R.color.expensesTextColor))
                itemView.priceCard.setCardBackgroundColor(resources.getColor(R.color.expensesColor))
            }
            val categoryImageUrl = this.resources.getIdentifier(
                "ic_cat_${category.img_res}",
                "drawable",
                packageName
            )
            //TODO
//            itemView.card
            if (category.id > 0) {
                try {
                    itemView.cardView.setCardBackgroundColor(
                        resources.getColor(
                            listOfColor[(category.id.minus(
                                1
                            ))]
                        )
                    )
                } catch (e: Exception) {
                    //apply random color
                    itemView.cardView.setCardBackgroundColor(
                        resources.getColor(
                            listOfColor[Random.nextInt(listOfColor.size)]
                        )
                    )
                }
            }
            requestManager
                ?.load(categoryImageUrl)
                ?.centerInside()
                ?.transition(withCrossFade())
                ?.error(R.drawable.ic_error)
                ?.into(itemView.category_image)
        }

    }


    class HeaderViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        private val currentLocale: Locale,
        private val _resources: Resources
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Record) = with(itemView) {

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
                itemView.header_expenses_sum.text = "${_resources.getString(R.string.expenses)}: ${
                    separate3By3(
                        item.money,
                        currentLocale
                    )
                }"
            } else {
                itemView.header_expenses_sum.text = ""

            }
            //cat_id for income
            if (item.incomeSum != null && item.incomeSum != 0.0) {
                itemView.header_income_sum.text = "${_resources.getString(R.string.income)}: ${
                    separate3By3(
                        item.incomeSum,
                        currentLocale
                    )
                }"
            } else {
                itemView.header_income_sum.text = ""
            }
            itemView.header_date.text = ""

            if (item.memo == TODAY) {
                itemView.header_date_name.text = _resources.getString(R.string.today)
            } else if (item.memo == YESTERDAY) {
                itemView.header_date_name.text = _resources.getString(R.string.yesterday)
            } else {
                try {
                    itemView.header_date_name.text = item.memo?.substring(
                        0, item.memo.indexOf(
                            DAY_OF_WEEK_MARKER
                        )
                    )
                    itemView.header_date.text =
                        item.memo?.substring(item.memo.indexOf(DAY_OF_WEEK_MARKER).plus(1))
                } catch (e: Exception) {
                    itemView.header_date.text = "${item.memo}"
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

    fun getCategoryById(id: Int): Category {
        categoryList.let {
            for (item in it) {
                if (item.id == id) {
                    return@let item
                }
            }
        }
        val category: Category = getCategoryByIdFromRoot(id)
        categoryList.add(category)
        return category
    }

    abstract fun getCategoryByIdFromRoot(id: Int): Category


    interface Interaction {

        fun onItemSelected(position: Int, item: Record)

        fun restoreListPosition()
    }
}