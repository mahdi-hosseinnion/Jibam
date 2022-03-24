package com.ssmmhh.jibam.ui.main.transaction.chart

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.LayoutChartListItemBinding
import com.ssmmhh.jibam.models.ChartData
import com.ssmmhh.jibam.util.localizeNumber
import com.ssmmhh.jibam.util.separate3By3
import java.util.*
import kotlin.math.abs
import kotlin.math.absoluteValue

//TODO DELETE DIFFUTIL FROM HERE
class ChartListAdapter(
    private val interaction: Interaction? = null,
    private val requestManager: RequestManager?,
    private val currentLocale: Locale,
    private val _resources: Resources,
    private val colors: List<Int>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var biggestPercentage: Float = 100.0f

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChartData>() {

        override fun areItemsTheSame(oldItem: ChartData, newItem: ChartData): Boolean =
            oldItem.categoryId == newItem.categoryId

        override fun areContentsTheSame(oldItem: ChartData, newItem: ChartData): Boolean =
            oldItem == newItem


    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return ChartViewHolder(
            LayoutChartListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            interaction, requestManager, currentLocale, _resources, colors
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChartViewHolder -> {
                holder.bind(differ.currentList.get(position), biggestPercentage)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<ChartData>) {
        if (!list.isNullOrEmpty()) {
            biggestPercentage = list.maxOf { abs(it.percentage) }
        }
        differ.submitList(list)
    }

    class ChartViewHolder
    constructor(
        val binding: LayoutChartListItemBinding,
        private val interaction: Interaction?,
        val requestManager: RequestManager?,
        val currentLocale: Locale,
        val _resources: Resources,
        val colors: List<Int>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChartData, biggestPercentage: Float) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }
            binding.txtDate.visibility = View.GONE

            binding.categoryName.text = item.getCategoryNameFromStringFile(context)
            binding.sumOfMoney.text = separate3By3(item.sumOfMoney.abs(), currentLocale)

            binding.txtPercentage.text =
                ("${item.percentage.toString()}%").localizeNumber(_resources)

            binding.prgPercentage.progress = item.percentage.toInt()
            binding.prgPercentage.max = biggestPercentage.toInt()

            val categoryImageResourceId = item.getCategoryImageResourceId(context)
            try {
                binding.cardView.setCardBackgroundColor((colors[adapterPosition]))
            } catch (e: Exception) {
                binding.cardView.setCardBackgroundColor(resources.getColor(R.color.category_list_item_image_background_color))
            }

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
