package com.ssmmhh.jibam.presentation.chart.chart

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.ChartData
import com.ssmmhh.jibam.databinding.LayoutChartListItemBinding
import com.ssmmhh.jibam.util.localizeNumber
import com.ssmmhh.jibam.util.separate3By3
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import kotlin.math.abs

@ExperimentalCoroutinesApi
@FlowPreview
class ChartListAdapter(
    private val viewModel: ChartViewModel,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data = emptyList<ChartData>()

    private var biggestPercentage: Float = 100.0f

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

    fun submitList(list: List<ChartData>) {
        if (!list.isNullOrEmpty()) {
            biggestPercentage = list.maxOf { abs(it.percentage) }
        }
        data = list
        notifyDataSetChanged()
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    class ChartViewHolder
    constructor(
        private val binding: LayoutChartListItemBinding,
        private val viewModel: ChartViewModel,
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(item: ChartData, biggestPercentage: Float) = with(binding) {
            this.viewmodel = viewModel
            this.item = item
            this.biggestPercentage = biggestPercentage.toInt()
        }
    }
}
