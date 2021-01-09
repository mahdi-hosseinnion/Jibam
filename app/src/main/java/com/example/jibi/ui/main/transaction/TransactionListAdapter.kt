package com.example.jibi.ui.main.transaction

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.util.GenericViewHolder
import kotlinx.android.synthetic.main.fragment_add_transaction.view.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.fragment_transaction.view.*
import kotlinx.android.synthetic.main.layout_new_transaction_list_item.view.*
import kotlinx.android.synthetic.main.layout_transacion_header.view.*
import kotlinx.android.synthetic.main.layout_transacion_header.view.header_date
import kotlinx.android.synthetic.main.layout_transacion_header.view.header_expenses_sum
import kotlinx.android.synthetic.main.layout_transacion_header.view.header_income_sum
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


abstract class TransactionListAdapter(
    private val requestManager: RequestManager?,
    private val interaction: Interaction? = null,
    private val packageName: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var categoryList = ArrayList<Category>()

    companion object {
        private const val TAG: String = "AppDebug"
        private const val NO_MORE_RESULTS = -1
        const val HEADER_ITEM = -3
        private const val BLOG_ITEM = 0
        private val NO_MORE_RESULTS_BLOG_MARKER = Record(
            NO_MORE_RESULTS,
            0,
            "",
            0,
            0
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
                    interaction = interaction
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
        if (position <= itemCount) {
            return differ.currentList[position].id < 0
        }
        return true
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
        transList: List<Record>?,
        isQueryExhausted: Boolean
    ) {
        val newList = transList?.toMutableList()
        if (isQueryExhausted)
            newList?.add(NO_MORE_RESULTS_BLOG_MARKER)
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
                itemView.main_text.text = category.name
            } else {
                itemView.main_text.text = item.memo
            }
            if (item.money >= 0) {
                itemView.price.text = "+$${separate3By3(item.money)}"
                itemView.price.setTextColor(resources.getColor(R.color.incomeTextColor))
                itemView.priceCard.setCardBackgroundColor(resources.getColor(R.color.incomeColor))
            } else {
                itemView.price.text = "-$${separate3By3(item.money)}"
                itemView.price.setTextColor(resources.getColor(R.color.expensesTextColor))
                itemView.priceCard.setCardBackgroundColor(resources.getColor(R.color.expensesColor))
            }
            val categoryImageUrl = this.resources.getIdentifier(
                "ic_cat_${category.name}",
                "drawable",
                packageName
            )
            //TODO
//            itemView.card
            itemView.cardView.setCardBackgroundColor(resources.getColor(listOfColor[(category.id+1)]))
            requestManager
                ?.load(categoryImageUrl)
                ?.centerInside()
                ?.transition(withCrossFade())
                ?.error(R.drawable.ic_error)
                ?.into(itemView.category_image)
            val ie = 1

        }

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

        private fun separate3By3(money1: Int): String {
            var money = money1
            if (money < 0) {
                money *= -1
            }
            if (money < 1000) {
                return money.toString()
            }
            val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
            formatter.applyPattern("#,###,###,###")
            return formatter.format(money)
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
            //hide margin for first object
//            if (adapterPosition<2){
//                val params = ((itemView.root_transaction_header).layoutParams) as RecyclerView.LayoutParams
//                params.setMargins(convertDpToPx(8), 0, convertDpToPx(8), 0) //substitute parameters for left, top, right, bottom
//
//                itemView.root_transaction_header.layoutParams = params
//            }
            //money for expenses
            Log.d(TAG, "bind: sum of all expenses = ${item.money}")
            if (item.money != 0) {
                itemView.header_expenses_sum.text = "Expenses: ${item.money}"
            } else {
                itemView.header_expenses_sum.text = ""

            }
            //cat_id for income
            if (item.cat_id != 0) {
                itemView.header_income_sum.text = "Income: ${item.cat_id}"
            } else {
                itemView.header_income_sum.text = ""
            }
            itemView.header_date.text = item.memo
//            itemView.header_date.text = DateUtils.convertLongToStringDate(item.date)
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

    inner class TransCardViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        val requestManager: RequestManager?
    ) : RecyclerView.ViewHolder(itemView), SubTransactionListAdapter.Interaction {

        private var recyclerAdapter: SubTransactionListAdapter? = null
        private val transactionContainer: LinearLayout =
            itemView.findViewById(R.id.card_linearLayout);

        fun bind(header: Record, items: List<Record>) = with(itemView) {
            //bind header
            if (header.money != 0) {
                itemView.header_expenses_sum.text = "Expenses: ${header.money}"
            } else {
                itemView.header_expenses_sum.text = ""

            }
            //cat_id for income
            if (header.cat_id != 0) {
                itemView.header_income_sum.text = "Income: ${header.cat_id}"
            } else {
                itemView.header_income_sum.text = ""
            }
            itemView.header_date.text = header.memo
            //bind items
            if (items.size < 6) {
                itemView.card_linearLayout.visibility = View.VISIBLE
                itemView.card_recycler_view.visibility = View.INVISIBLE
                bindWithLinearLayout(items)
            } else {
                itemView.card_linearLayout.visibility = View.INVISIBLE
                itemView.card_recycler_view.visibility = View.VISIBLE
                initRecyclerView()
                submitListToRecyclerView(items)
            }
        }

        private fun bindWithLinearLayout(items: List<Record>) {
            val view: View = LayoutInflater.from(itemView.context)
                .inflate(R.layout.layout_transaction_list_item, null)
//            val view: View = LayoutInflater.from(itemView.context).inflate(
//                R.layout.layout_transaction_list_item,
//                (itemView.getParent() as ViewGroup),
//                false
//            )
            transactionContainer.removeAllViews()
            for (i in items.indices) {
                val childItem = items[i]
                var childCategory = getCategoryById(childItem.cat_id)
                //hide the divider
//                if (items.size == i.plus(1)) {
//                    view.transaction_divider.visibility = View.GONE
//                } else {
//                    view.transaction_divider.visibility = View.VISIBLE
//                }
                //set the texts
                if (childItem.memo.isNullOrBlank()) {
                    view.main_text.text = childCategory.name
                } else {
                    view.main_text.text = childItem.memo
                }
                view.price.text = "${separate3By3(childItem.money)}"
                if (childItem.money >= 0) {
                    view.price.setTextColor(itemView.resources.getColor(R.color.incomeTextColor))
                    view.priceCard.setCardBackgroundColor(itemView.resources.getColor(R.color.incomeColor))
                } else {
                    view.price.setTextColor(itemView.resources.getColor(R.color.expensesTextColor))
                    view.priceCard.setCardBackgroundColor(itemView.resources.getColor(R.color.expensesColor))
                }
                //add to linearLayout
                if (view.getParent() != null) {
                    (view.getParent() as ViewGroup).removeView(view) // <- fix
                }
                transactionContainer.addView(view)

            }
        }

        private fun separate3By3(money1: Int): String {
            var money = money1
            if (money < 0) {
                money *= -1
            }
            if (money < 1000) {
                return money.toString()
            }
            val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
            formatter.applyPattern("#,###,###,###")
            return formatter.format(money)
        }

        fun submitListToRecyclerView(items: List<Record>) {
            recyclerAdapter?.submitList(items)
        }

        private fun initRecyclerView() {

            itemView.card_recycler_view.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                recyclerAdapter = SubTransactionListAdapter(
                    null,
                    this@TransCardViewHolder
                )

                adapter = recyclerAdapter
            }

        }

        override fun onItemSelected(position: Int, item: Record) {
        }

    }


    interface Interaction {

        fun onItemSelected(position: Int, item: Record)

        fun restoreListPosition()
    }
}