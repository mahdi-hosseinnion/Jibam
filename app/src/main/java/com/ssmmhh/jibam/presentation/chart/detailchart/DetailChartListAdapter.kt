package com.ssmmhh.jibam.presentation.chart.detailchart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.databinding.LayoutChartDetailListItemBinding
import com.ssmmhh.jibam.util.GenericViewHolder
import com.ssmmhh.jibam.util.calculatePercentage
import com.ssmmhh.jibam.util.calculatePercentageAndRoundResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.math.BigDecimal

@ExperimentalCoroutinesApi
@FlowPreview
class DetailChartListAdapter(
    private val viewModel: DetailChartViewModel,
    private val isCalendarSolar: Boolean,
    private var data: List<TransactionDto>? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var totalAmount: BigDecimal = data?.sumOf { (it.money).abs() } ?: BigDecimal.ZERO
    private var biggestAmount: BigDecimal = data?.maxOf { (it.money).abs() } ?: BigDecimal.ZERO

    class DetailChartViewHolder(
        private val binding: LayoutChartDetailListItemBinding,
        private val viewModel: DetailChartViewModel,
        private val _isCalendarSolar: Boolean,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: TransactionDto,
            totalAmount: BigDecimal,
            biggestAmount: BigDecimal
        ) = with(binding) {
            this.viewmodel = viewModel
            this.item = item
            this.isCalendarSolar = _isCalendarSolar
            this.percentage = calculatePercentageAndRoundResult(item.money, totalAmount).toFloat()
            this.progressBarMaximum = calculatePercentage(biggestAmount, totalAmount).toInt()

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

