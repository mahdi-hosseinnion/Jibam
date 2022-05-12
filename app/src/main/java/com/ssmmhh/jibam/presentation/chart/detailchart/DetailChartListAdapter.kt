package com.ssmmhh.jibam.presentation.chart.detailchart

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.databinding.LayoutChartDetailListItemBinding
import com.ssmmhh.jibam.util.*
import java.math.BigDecimal
import java.util.*

class DetailChartListAdapter(
    private val interaction: Interaction? = null,
    private val isCalendarSolar: Boolean,
    private var requestManager: RequestManager,
    private var currentLocale: Locale,
    private var data: List<TransactionDto>? = null

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var totalAmount: BigDecimal = data?.sumOf { (it.money).abs() } ?: BigDecimal.ZERO
    private var biggestAmount: BigDecimal = data?.maxOf { (it.money).abs() } ?: BigDecimal.ZERO

    companion object {

        private const val EMPTY_LIST_MARKER = -2
        private val EMPTY_LIST_MARKER_TRANSACTION = TransactionDto(
            id = EMPTY_LIST_MARKER,
            BigDecimal.ZERO,
            "",
            0,
            "",
            "",
            "",
            0
        )

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == EMPTY_LIST_MARKER) {
            GenericViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_detail_chart_empty_list_item, parent, false),
                R.id.info_text,
                R.string.no_transaction_found_with_this_category
            )
        } else {
            DetailChartViewHolder(
                binding = LayoutChartDetailListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                interaction,
                isCalendarSolar,
                requestManager,
                currentLocale
            )
        }
    }

    override fun getItemCount() = data?.size ?: 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DetailChartViewHolder) {
            val item = data?.get(position) ?: return
            holder.bind(item, totalAmount, biggestAmount)
        }
    }

    fun swapData(data: List<TransactionDto>?) {
        if (data.isNullOrEmpty()) {
            this.data = arrayListOf(EMPTY_LIST_MARKER_TRANSACTION)
        } else {
            this.data = data
            totalAmount = data.sumOf { (it.money).abs() }
            biggestAmount = data.maxOf { (it.money).abs() }
        }
        notifyDataSetChanged()
    }

    fun getTransactionAt(position: Int): TransactionDto? = this.data?.get(index = position)


    class DetailChartViewHolder(
        val binding: LayoutChartDetailListItemBinding,
        private val interaction: Interaction?,
        private val isCalendarSolar: Boolean,
        private var requestManager: RequestManager,
        private var currentLocale: Locale
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: TransactionDto,
            totalAmount: BigDecimal,
            biggestAmount: BigDecimal
        ) = with(itemView) {
            loadImage(item)
            showPercentage(item.money, totalAmount, biggestAmount)
            //set text
            if (item.memo.isNullOrBlank()) {
                binding.categoryName.text = item.getCategoryNameFromStringFile(context)
            } else {
                binding.categoryName.text = item.memo
            }
            binding.sumOfMoney.text = (item.money).abs().toString().localizeNumber(resources)
            binding.txtDate.visibility = View.VISIBLE
            binding.txtDate.text = dateWithPattern(item.date)

            setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }
        }


        private fun loadImage(item: TransactionDto) {

            binding.cardView.setCardBackgroundColor(
                Color.parseColor(item.categoryImageBackgroundColor)
            )

            val categoryImageResourceId = item.getCategoryImageResourceId(itemView.context)
            requestManager
                .load(categoryImageResourceId)
                .centerInside()
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.ic_error)
                .into(binding.categoryImg)
        }

        private fun showPercentage(
            money: BigDecimal,
            totalAmount: BigDecimal,
            biggestAmount: BigDecimal
        ) {
            val percentage = calculatePercentageAndRoundResult(
                money,
                totalAmount
            )

            binding.txtPercentage.text =
                ("${percentage}%").localizeNumber(
                    itemView.resources
                )
            binding.prgPercentage.progress = percentage.toInt()
            binding.prgPercentage.max = calculatePercentage(biggestAmount, totalAmount).toInt()
        }

        private fun dateWithPattern(date: Long): String {
            val dateHolder = DateUtils.convertUnixTimeToDate(date, isCalendarSolar)
            return if (isCalendarSolar) {
                "${dateHolder.year.toLocaleString()}/${dateHolder.month.toLocaleStringWithTwoDigits()}/${dateHolder.day.toLocaleStringWithTwoDigits()}"
            } else {
                "${dateHolder.month.toLocaleStringWithTwoDigits()}/${dateHolder.day.toLocaleStringWithTwoDigits()}/${dateHolder.year.toLocaleString()}"
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (data?.get(position)?.id == EMPTY_LIST_MARKER) {
            return EMPTY_LIST_MARKER
        }
        return super.getItemViewType(position)
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: TransactionDto)
    }


}

