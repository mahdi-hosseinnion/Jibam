package com.ssmmhh.jibam.presentation.chart.chart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssmmhh.jibam.data.model.ChartData
import com.ssmmhh.jibam.databinding.LayoutChartListItemBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlin.math.abs

@ExperimentalCoroutinesApi
@FlowPreview
class ChartListAdapter(
    private val viewModel: ChartViewModel,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data = emptyList<ChartData>()

    private var biggestPercentage: Int = 100

    class ChartViewHolder(
        private val binding: LayoutChartListItemBinding,
        private val viewModel: ChartViewModel,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChartData, biggestPercentage: Int) = with(binding) {
            this.viewmodel = viewModel
            this.item = item
            this.biggestPercentage = biggestPercentage
        }
    }

    fun submitList(list: List<ChartData>) {
        if (!list.isNullOrEmpty()) {
            biggestPercentage = list.maxOf { abs(it.percentage) }.toInt().coerceAtMost(100)
        }
        data = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return ChartViewHolder(
            LayoutChartListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            viewModel,
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChartViewHolder -> {
                holder.bind(data[position], biggestPercentage)
            }
        }
    }

    override fun getItemCount(): Int = data.size

}
