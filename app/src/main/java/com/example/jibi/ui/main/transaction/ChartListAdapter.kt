package com.example.jibi.ui.main.transaction

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
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.category_image
import java.util.*
import kotlin.math.absoluteValue

class ChartListAdapter(
    private val interaction: Interaction? = null,
    private val requestManager: RequestManager?,
    private val currentLocale: Locale,
    private val packageName: String,
    private val _resources: Resources,
    private val colors: List<Int>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PieChartData>() {

        override fun areItemsTheSame(oldItem: PieChartData, newItem: PieChartData): Boolean =
            (oldItem.sumOfMoney == newItem.sumOfMoney && oldItem.categoryName == newItem.categoryName)

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
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<PieChartData>) {
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

        fun bind(item: PieChartData) = with(itemView) {
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
            prg_percentage.max = 100

            val categoryImageUrl = this.resources.getIdentifier(
                "ic_cat_${item.categoryImage}",
                "drawable",
                packageName
            )

            itemView.cardView.setCardBackgroundColor((colors[adapterPosition]))

            requestManager
                ?.load(categoryImageUrl)
                ?.centerInside()
                ?.transition(DrawableTransitionOptions.withCrossFade())
                ?.error(R.drawable.ic_error)
                ?.into(itemView.category_image)

        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: PieChartData)
    }
}
