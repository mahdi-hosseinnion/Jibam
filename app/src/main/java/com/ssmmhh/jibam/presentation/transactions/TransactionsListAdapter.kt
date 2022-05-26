package com.ssmmhh.jibam.presentation.transactions

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
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
import com.ssmmhh.jibam.util.DateUtils
import com.ssmmhh.jibam.util.DateUtils.DAY_IN_SECONDS
import com.ssmmhh.jibam.util.GenericViewHolder
import com.ssmmhh.jibam.util.getStringFromItemView
import com.ssmmhh.jibam.util.toLocaleString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class TransactionsListAdapter(
    private val viewModel: TransactionsViewModel,
    private val isCalendarSolar: Boolean,
) : ListAdapter<TransactionsRecyclerViewItem, RecyclerView.ViewHolder>(TransactionDiffCallback()) {

    companion object {
        private const val TAG: String = "TransactionsListAdapter"
    }

    /**
     * Represent the exact unix time of current date at (00:00). used to show 'Today' in header text.
     */
    private val startOfTodayInSeconds: Long by lazy {
        return@lazy DateUtils.getTheMidnightOfDateInSecond(
            unixTimeInSeconds = DateUtils.getCurrentUnixTimeInSeconds()
        )
    }
    private val startOfYesterday: Long by lazy {
        return@lazy startOfTodayInSeconds.minus(DAY_IN_SECONDS)
    }
    private val startOfTomorrow: Long by lazy {
        return@lazy startOfTodayInSeconds.plus(DAY_IN_SECONDS)
    }

    class TransViewHolder(
        private val binding: LayoutTransactionListItemBinding,
        private val viewModel: TransactionsViewModel,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: Transaction,
            isNextItemHeader: Boolean = false
        ) = with(binding) {
            this.viewmodel = viewModel
            this.item = item
            this.isNextItemHeader = isNextItemHeader
        }

    }

    class HeaderViewHolder(
        private val binding: LayoutTransacionHeaderBinding,
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

    /**
     * Remove transaction at [position].
     * Remove transaction's header if its the last transaction with that header.
     */
    fun removeItemAt(position: Int): TransactionDto? {
        val newList = currentList.toMutableList()

        val previousPosition = position.minus(1)
        val nextPosition = position.plus(1)
        val previousItem = currentList.getOrNull(previousPosition)
        val nextItem = currentList.getOrNull(nextPosition)

        val deleteItem = newList.removeAt(position) ?: return null
        //Remove header
        if (previousItem is TransactionsRecyclerViewItem.Header &&
            nextItem !is TransactionsRecyclerViewItem.TransactionItem
        ) {
            newList.remove(previousItem)
        }

        submitList(newList)
        return if (deleteItem is TransactionsRecyclerViewItem.TransactionItem) deleteItem.toTransaction() else null

    }

    private fun isHeader(position: Int): Boolean =
        if (position < itemCount)
            currentList[position].isHeader
        else true

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
                    viewModel = viewModel,
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
                    startOfToday = startOfTodayInSeconds,
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
                    viewModel = viewModel,
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TransViewHolder -> {
                val item = currentList[position]
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
                val item = currentList[position]
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

    override fun getItemViewType(position: Int): Int = currentList[position].itemType

    override fun getItemCount(): Int = currentList.size

}

/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minimum number of changes between and old list and a new
 * list that's been passed to `submitList`.
 */
class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionsRecyclerViewItem>() {
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
        else if (
            oldItem is TransactionsRecyclerViewItem.Header
            &&
            newItem is TransactionsRecyclerViewItem.Header
        )
            oldItem.date == newItem.date
        else
            oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: TransactionsRecyclerViewItem,
        newItem: TransactionsRecyclerViewItem
    ): Boolean {
        return oldItem == newItem
    }
}