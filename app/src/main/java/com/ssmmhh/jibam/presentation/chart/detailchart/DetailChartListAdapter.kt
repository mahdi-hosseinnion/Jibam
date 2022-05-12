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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.math.BigDecimal

@ExperimentalCoroutinesApi
@FlowPreview
class DetailChartListAdapter(
    private val viewModel: DetailChartViewModel,
    private val isCalendarSolar: Boolean,
    private val requestManager: RequestManager,
    private var data: List<TransactionDto>? = null

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var totalAmount: BigDecimal = data?.sumOf { (it.money).abs() } ?: BigDecimal.ZERO
    private var biggestAmount: BigDecimal = data?.maxOf { (it.money).abs() } ?: BigDecimal.ZERO

    class DetailChartViewHolder(
        private val binding: LayoutChartDetailListItemBinding,
        private val viewModel: DetailChartViewModel,
        private val isCalendarSolar: Boolean,
        private var requestManager: RequestManager,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: TransactionDto,
            totalAmount: BigDecimal,
            biggestAmount: BigDecimal
        ) = with(binding) {
            this.viewmodel = viewModel
            this.item = item
            loadImage(item)
            showPercentage(item.money, totalAmount, biggestAmount)
            //set text
            if (item.memo.isNullOrBlank()) {
                categoryName.text = item.getCategoryNameFromStringFile(binding.root.context)
            } else {
                categoryName.text = item.memo
            }
            sumOfMoney.text = (item.money).abs().toString().localizeNumber(binding.root.resources)
            txtDate.visibility = View.VISIBLE
            txtDate.text = dateWithPattern(item.date)

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

    fun submitData(data: List<TransactionDto>?) {
        if (data.isNullOrEmpty()) {
            this.data = arrayListOf(EMPTY_LIST_MARKER_TRANSACTION)
        } else {
            this.data = data
            totalAmount = data.sumOf { (it.money).abs() }
            biggestAmount = data.maxOf { (it.money).abs() }
        }
        notifyDataSetChanged()
    }

    fun getTransactionAt(position: Int): TransactionDto? = this.data?.getOrNull(index = position)

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
                viewModel,
                isCalendarSolar,
                requestManager,
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DetailChartViewHolder) {
            val item = data?.get(position) ?: return
            holder.bind(item, totalAmount, biggestAmount)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (data?.get(position)?.id == EMPTY_LIST_MARKER) {
            return EMPTY_LIST_MARKER
        }
        return super.getItemViewType(position)
    }

    override fun getItemCount() = data?.size ?: 0

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
}

