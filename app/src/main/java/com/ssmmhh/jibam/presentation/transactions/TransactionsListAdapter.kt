package com.ssmmhh.jibam.presentation.transactions

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.DateHolder
import com.ssmmhh.jibam.data.model.Transaction
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.databinding.LayoutTransacionHeaderBinding
import com.ssmmhh.jibam.databinding.LayoutTransactionListItemBinding
import com.ssmmhh.jibam.presentation.transactions.TransactionsRecyclerViewItem.Companion.DATABASE_IS_EMPTY_VIEW_TYPE
import com.ssmmhh.jibam.presentation.transactions.TransactionsRecyclerViewItem.Companion.HEADER_VIEW_TYPE
import com.ssmmhh.jibam.presentation.transactions.TransactionsRecyclerViewItem.Companion.NO_MORE_RESULT_VIEW_TYPE
import com.ssmmhh.jibam.presentation.transactions.TransactionsRecyclerViewItem.Companion.NO_RESULT_FOUND_VIEW_TYPE
import com.ssmmhh.jibam.presentation.transactions.TransactionsRecyclerViewItem.Companion.TRANSACTION_VIEW_TYPE
import com.ssmmhh.jibam.util.*
import com.ssmmhh.jibam.util.DateUtils.DAY_IN_SECONDS
import java.math.BigDecimal
import java.util.*


class TransactionsListAdapter(
    private val requestManager: RequestManager?,
    private val interaction: Interaction? = null,
    private val currentLocale: Locale,
    private val isCalendarSolar: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TAG: String = "TransactionsListAdapter"
    }

    private val startOfToday: Long by lazy {
        return@lazy DateUtils.getTheMidnightOfDay(
            unixTimeInSeconds = DateUtils.getCurrentUnixTimeInSeconds()
        )
    }
    private val startOfYesterday: Long by lazy {
        return@lazy DateUtils.getTheMidnightOfDay(
            unixTimeInSeconds = DateUtils.getCurrentUnixTimeInSeconds().minus(
                DAY_IN_SECONDS
            )
        )
    }
    private val startOfTomorrow: Long by lazy {
        return@lazy DateUtils.getTheMidnightOfDay(
            unixTimeInSeconds = DateUtils.getCurrentUnixTimeInSeconds().plus(
                DAY_IN_SECONDS
            )
        )
    }
    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionsRecyclerViewItem>() {

        override fun areItemsTheSame(
            oldItem: TransactionsRecyclerViewItem,
            newItem: TransactionsRecyclerViewItem
        ): Boolean {
            return if (
                oldItem is TransactionsRecyclerViewItem.TransactionItem
                &&
                newItem is TransactionsRecyclerViewItem.TransactionItem
            )
                oldItem.transaction.id == newItem.transaction.id
            else
                false
        }

        override fun areContentsTheSame(
            oldItem: TransactionsRecyclerViewItem,
            newItem: TransactionsRecyclerViewItem
        ): Boolean {
            return oldItem == newItem
        }

    }
    private val differ =
        AsyncListDiffer(
            BlogRecyclerChangeCallback(this),
            AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
        )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            NO_MORE_RESULT_VIEW_TYPE -> {
                GenericViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_no_more_results,
                        parent,
                        false
                    ),
                    R.id.big_JIBI,
                    R.string.jibam_capital
                )
            }
            NO_RESULT_FOUND_VIEW_TYPE -> {
                Log.e(TAG, "onCreateViewHolder: NO result  found with this query or filter ...")
                GenericViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_no_results_found_list_item,
                        parent,
                        false
                    ),
                    R.id.no_result_found,
                    R.string.no_result_found_for_this_search
                )
            }
            DATABASE_IS_EMPTY_VIEW_TYPE -> {
                Log.e(TAG, "onCreateViewHolder: Database is empty...")
                GenericViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_no_results_found_in_database_list_item,
                        parent,
                        false
                    ),
                    R.id.nullTRANSACTION,
                    R.string.insert_some_transaction_with_add_button
                )
            }
            TRANSACTION_VIEW_TYPE -> {
                TransViewHolder(
                    binding = LayoutTransactionListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager,
                    currentLocale = currentLocale,
                )
            }
            HEADER_VIEW_TYPE -> {
                HeaderViewHolder(
                    binding = LayoutTransacionHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    isCalendarSolar = isCalendarSolar,
                    startOfToday = startOfToday,
                    startOfYesterday = startOfYesterday,
                    startOfTomorrow = startOfTomorrow,
                )
            }
            else -> {
                TransViewHolder(
                    binding = LayoutTransactionListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager,
                    currentLocale = currentLocale,
                )
            }
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
                val item = differ.currentList[position]
                if (item is TransactionsRecyclerViewItem.TransactionItem)
                    holder.bind(item.transaction, isHeader(position.plus(1)))
                else {
                    Log.e(
                        TAG, "onBindViewHolder: View holder is TransViewHolder but the item" +
                                "at position: $position is not. item at position: $item"
                    )
                }
            }
            is HeaderViewHolder -> {
                val item = differ.currentList[position]
                if (item is TransactionsRecyclerViewItem.Header)
                    holder.bind(item)
                else {
                    Log.e(
                        TAG, "onBindViewHolder: View holder is HeaderViewHolder but the item" +
                                "at position: $position is not. item at position: $item"
                    )
                }
            }
        }
    }

    private fun isHeader(position: Int): Boolean =
        if (position < itemCount)
            differ.currentList[position].isHeader
        else true


    override fun getItemViewType(position: Int): Int {
        return differ.currentList[position].itemType
    }

    override fun getItemCount(): Int = differ.currentList.size

    /**
     * Remove transaction at [position].
     * Remove transaction's header if its the last transaction with that header.
     */
    fun removeItemAt(position: Int): TransactionDto? {
        val newList = differ.currentList.toMutableList()

        val previousPosition = position.minus(1)
        val nextPosition = position.plus(1)
        val previousItem = differ.currentList.getOrNull(previousPosition)
        val nextItem = differ.currentList.getOrNull(nextPosition)

        val deleteItem = newList.removeAt(position) ?: return null
        //Remove header
        if (previousItem is TransactionsRecyclerViewItem.Header &&
            nextItem !is TransactionsRecyclerViewItem.TransactionItem
        ) {
            newList.remove(previousItem)
        }

        differ.submitList(newList)
        return if (deleteItem is TransactionsRecyclerViewItem.TransactionItem) deleteItem.toTransaction() else null

    }

    fun submitList(
        transList: List<TransactionsRecyclerViewItem>?,
    ) {
        val newList = transList?.toMutableList()
        if (
            newList?.get(0) !is TransactionsRecyclerViewItem.NoResultFound
            &&
            newList?.get(0) !is TransactionsRecyclerViewItem.DatabaseIsEmpty
        ) {
            if ((newList?.size ?: 0) > 10) {
                newList?.add(TransactionsRecyclerViewItem.NoMoreResult)
            }
        }
        val commitCallback = Runnable {
            // if process died must restore list position
            // very annoying
            interaction?.restoreListPosition()
        }
        differ.submitList(newList, commitCallback)
    }

    class TransViewHolder
    constructor(
        val binding: LayoutTransactionListItemBinding,
        val requestManager: RequestManager?,
        private val currentLocale: Locale,
        private val interaction: Interaction?,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: Transaction,
            isNextItemHeader: Boolean = false
        ) = with(binding) {

            itemView.setOnClickListener {
                interaction?.onClickedOnTransaction(adapterPosition, item)
            }

            if (isNextItemHeader) {
                transactionDivider.visibility = View.GONE
            } else {
                transactionDivider.visibility = View.VISIBLE
            }

            if (item.memo.isNullOrBlank()) {
                mainText.text = item.getCategoryNameFromStringFile(itemView.context)
            } else {
                mainText.text = item.memo
            }
            if (item.money >= BigDecimal.ZERO) {
                //income
                price.text = separate3By3(item.money, currentLocale)
                price.setTextColor(itemView.resources.getColor(R.color.blue_500))
            } else {
                //expenses
                price.text = separate3By3(item.money, currentLocale)
                price.setTextColor(itemView.resources.getColor(R.color.red_500))
            }

            cardView.setCardBackgroundColor(
                Color.parseColor(item.categoryImage.backgroundColor)
            )
            val categoryImageResourceId = item.categoryImage.getImageResourceId(itemView.context)
            requestManager
                ?.load(categoryImageResourceId)
                ?.centerInside()
                ?.transition(withCrossFade())
                ?.error(R.drawable.ic_error)
                ?.into(categoryImage)
        }

    }


    class HeaderViewHolder
    constructor(
        val binding: LayoutTransacionHeaderBinding,
        private val isCalendarSolar: Boolean,
        private val startOfToday: Long,
        private val startOfYesterday: Long,
        private val startOfTomorrow: Long,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionsRecyclerViewItem.Header) = with(binding) {
            this.item = item
            var date = ""
            val dayOfWeek = when (item.date) {
                //Yesterday unix time range
                in startOfYesterday until startOfToday -> {
                    getStringFromItemView(R.string.yesterday)
                }
                //Today unix time range
                in startOfToday until startOfTomorrow -> {
                    getStringFromItemView(R.string.today)
                }
                else -> {
                    val dateHolder = DateUtils.convertUnixTimeToDate(item.date, isCalendarSolar)
                    date = getFormattedDate(dateHolder)
                    dateHolder.getDayOfWeekName(itemView.context.resources) + ","
                }
            }
            this.date = date
            this.dayOfWeek = dayOfWeek
        }

        private fun getFormattedDate(date: DateHolder): String {
            return if (isCalendarSolar) {
                "${date.day.toLocaleString()} ${date.getAbbreviationFormOfMonthName(itemView.context.resources)}"
            } else {
                "${date.day.toLocaleString()} ${date.getAbbreviationFormOfMonthName(itemView.context.resources)}"
            }
        }
    }


    interface Interaction {

        fun onClickedOnTransaction(position: Int, item: Transaction)

        fun restoreListPosition()
    }
}