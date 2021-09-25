package com.example.jibi.ui.main.transaction.chart

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.jibi.R
import com.example.jibi.models.PieChartData
import com.example.jibi.util.localizeNumber
import com.example.jibi.util.separate3By3
import kotlinx.android.synthetic.main.layout_chart_list_item.view.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.cardView
import java.lang.Exception
import java.util.*
import kotlin.math.abs
import kotlin.math.absoluteValue

//TODO DELETE DIFFUTIL FROM HERE
class ChartListAdapter(
    private val interaction: Interaction? = null,
    private val requestManager: RequestManager?,
    private val currentLocale: Locale,
    private val packageName: String,
    private val _resources: Resources,
    private val colors: List<Int>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var biggestPercentage: Double = 100.0

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PieChartData>() {

        override fun areItemsTheSame(oldItem: PieChartData, newItem: PieChartData): Boolean =
            oldItem.categoryId == newItem.categoryId

        override fun areContentsTheSame(oldItem: PieChartData, newItem: PieChartData): Boolean =
            oldItem == newItem


    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return ChartViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_chart_list_item,
                parent,
                false
            ),
            interaction, requestManager, packageName, currentLocale, _resources, colors
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

    fun submitList(list: List<PieChartData>) {
        if (!list.isNullOrEmpty()) {
            biggestPercentage = list.maxOf { abs(it.percentage) }
        }
        differ.submitList(list)
    }

    class ChartViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        val requestManager: RequestManager?,
        val packageName: String,
        val currentLocale: Locale,
        val _resources: Resources,
        val colors: List<Int>
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: PieChartData, biggestPercentage: Double) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            category_name.text = item.getCategoryNameFromStringFile(
                _resources,
                this@ChartViewHolder.packageName
            ) {
                it.categoryName
            }
            sumOfMoney.text = separate3By3(item.sumOfMoney.absoluteValue, currentLocale)

            txt_percentage.text = ("${item.percentage.toString()}%").localizeNumber(_resources)
            prg_percentage.progress = item.percentage?.toInt() ?: 0
            prg_percentage.max = biggestPercentage.toInt()

            val categoryImageUrl = this.resources.getIdentifier(
                "ic_cat_${item.categoryImage}",
                "drawable",
                packageName
            )
            try{
                itemView.cardView.setCardBackgroundColor((colors[adapterPosition]))
            }catch (e:Exception){
                itemView.cardView.setCardBackgroundColor(resources.getColor(R.color.category_list_item_image_background_color))
            }

            requestManager
                ?.load(categoryImageUrl)
                ?.centerInside()
                ?.transition(DrawableTransitionOptions.withCrossFade())
                ?.error(R.drawable.ic_error)
                ?.into(itemView.category_img)

        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: PieChartData)
    }
}
