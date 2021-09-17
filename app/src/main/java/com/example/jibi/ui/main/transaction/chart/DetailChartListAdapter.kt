package com.example.jibi.ui.main.transaction.chart

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.Transaction
import com.example.jibi.ui.main.transaction.transactions.TransactionsListAdapter
import com.example.jibi.util.*
import kotlinx.android.synthetic.main.layout_chart_list_item.view.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.cardView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

class DetailChartListAdapter(
    private val interaction: Interaction? = null,
    private var packageName: String,
    private var requestManager: RequestManager,
    private var resources: Resources,
    private var currentLocale: Locale,
    private var data: List<Transaction> = ArrayList()

) : RecyclerView.Adapter<DetailChartListAdapter.DetailChartViewHolder>() {

    private var totalAmount: Double = data.sumOf { abs(it.money) }
    private var biggestAmount: Double = data.maxOf { abs(it.money) }

    companion object {
        private const val DATE_PATTERN = "MM/dd/yyyy"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailChartViewHolder {
        return DetailChartViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_chart_list_item, parent, false),
            interaction,
            packageName,
            requestManager,
            resources,
            currentLocale
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: DetailChartViewHolder, position: Int) =
        holder.bind(data[position], totalAmount, biggestAmount)

    fun swapData(data: List<Transaction>) {
        this.data = data
        totalAmount = data.sumOf { abs(it.money) }
        biggestAmount = data.maxOf { abs(it.money) }
        notifyDataSetChanged()
    }

    fun getTransaction(position: Int): Transaction? = this.data.get(index = position)


    class DetailChartViewHolder(
        itemView: View,
        private val interaction: Interaction?,
        private var packageName: String,
        private var requestManager: RequestManager,
        private var _resources: Resources,
        private var currentLocale: Locale
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            item: Transaction,
            totalAmount: Double,
            biggestAmount: Double
        ) = with(itemView) {
            loadImage(item.categoryId, item.categoryImage)
            showPercentage(item.money, totalAmount, biggestAmount)
            //set text
            if (item.memo.isNullOrBlank()) {
                itemView.category_name.text = item.getCategoryNameFromStringFile(
                    _resources,
                    packageName
                ) {
                    it.categoryName
                }
            } else {
                itemView.category_name.text = item.memo
            }
            sumOfMoney.text = abs(item.money).toString().localizeNumber(_resources)

            txt_date.text = dateWithPattern(item.date)

            setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }
        }


        private fun loadImage(categoryId: Int, categoryImage: String) {
            if (categoryId > 0) {
                try {
                    itemView.cardView.setCardBackgroundColor(
                        _resources.getColor(
                            TransactionsListAdapter.listOfColor[(categoryId.minus(
                                1
                            ))]
                        )
                    )
                } catch (e: Exception) {
                    //apply random color
                    itemView.cardView.setCardBackgroundColor(
                        _resources.getColor(
                            TransactionsListAdapter.listOfColor
                                    [Random.nextInt(TransactionsListAdapter.listOfColor.size)]
                        )
                    )
                }
            }
            val categoryImageUrl = this._resources.getIdentifier(
                "ic_cat_${categoryImage}",
                "drawable",
                packageName
            )
            requestManager
                .load(categoryImageUrl)
                .centerInside()
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.ic_error)
                .into(itemView.category_img)
        }

        private fun showPercentage(
            money: Double,
            totalAmount: Double,
            biggestAmount: Double
        ) {
            val percentage = calculatePercentageAndRoundResult(
                money,
                totalAmount
            )

            itemView.txt_percentage.text =
                ("${percentage}%").localizeNumber(
                    _resources
                )
            itemView.prg_percentage.progress = percentage.toInt()
            itemView.prg_percentage.max = calculatePercentage(biggestAmount, totalAmount).toInt()
        }

        private fun dateWithPattern(date: Int): String {
            return if (currentLocale.isFarsi()) {
                SolarCalendar.calcSolarCalendar(
                    date.times(1000L),
                    SolarCalendar.ShamsiPatterns.DETAIL_CHART_FRAGMENT, currentLocale
                )
            } else {
                val df = Date(date.times(1000L))
                SimpleDateFormat(DATE_PATTERN, currentLocale).format(df)
            }
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: Transaction)
    }
}