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
import java.util.*
import kotlin.math.abs

class ChartListAdapter(
    private val interaction: Interaction? = null,
    private val requestManager: RequestManager?,
    private val currentLocale: Locale,
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
            interaction, requestManager, currentLocale
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

    class ChartViewHolder
    constructor(
        val binding: LayoutChartListItemBinding,
        private val interaction: Interaction?,
        val requestManager: RequestManager?,
        val currentLocale: Locale,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChartData, biggestPercentage: Float) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }
            binding.txtDate.visibility = View.GONE

            binding.categoryName.text = item.getCategoryNameFromStringFile(context)
            binding.sumOfMoney.text = separate3By3(item.sumOfMoney.abs(), currentLocale)

            binding.txtPercentage.text =
                ("${item.percentage.toString()}%").localizeNumber(resources)

            binding.prgPercentage.progress = item.percentage.toInt()
            binding.prgPercentage.max = biggestPercentage.toInt()

            val categoryImageResourceId = item.getCategoryImageResourceId(context)

            binding.cardView.setCardBackgroundColor(
                Color.parseColor(item.categoryImage.backgroundColor)
            )

            requestManager
                ?.load(categoryImageResourceId)
                ?.centerInside()
                ?.transition(DrawableTransitionOptions.withCrossFade())
                ?.error(R.drawable.ic_error)
                ?.into(binding.categoryImg)

        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: ChartData)
    }
}
