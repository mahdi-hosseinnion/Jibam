package com.ssmmhh.jibam.ui.main.transaction.chart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.models.Transaction
import com.ssmmhh.jibam.util.*
import kotlinx.android.synthetic.main.layout_chart_list_item.view.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.cardView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class DetailChartListAdapter(
    private val interaction: Interaction? = null,
    private var packageName: String,
    private val isCalendarSolar: Boolean,
    private var requestManager: RequestManager,
    private var currentLocale: Locale,
    private var data: List<Transaction>? = null

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var totalAmount: Double = data?.sumOf { abs(it.money) } ?: 0.0
    private var biggestAmount: Double = data?.maxOf { abs(it.money) } ?: 0.0

    companion object {
        private const val DATE_PATTERN = "MM/dd/yyyy"

        private const val EMPTY_LIST_MARKER = -2
        private val EMPTY_LIST_MARKER_TRANSACTION = Transaction(
            id = EMPTY_LIST_MARKER,
            0.0,
            "",
            0.0
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
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_chart_list_item, parent, false),
                interaction,
                isCalendarSolar,
                packageName,
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

    fun swapData(data: List<Transaction>?) {
        if (data.isNullOrEmpty()) {
            this.data = arrayListOf(EMPTY_LIST_MARKER_TRANSACTION)
        } else {
            this.data = data
            totalAmount = data.sumOf { abs(it.money) }
            biggestAmount = data.maxOf { abs(it.money) }
        }
        notifyDataSetChanged()
    }

    fun getTransaction(position: Int): Transaction? = this.data?.get(index = position)


    class DetailChartViewHolder(
        itemView: View,
        private val interaction: Interaction?,
        private val isCalendarSolar: Boolean,
        private var packageName: String,
        private var requestManager: RequestManager,
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
                    itemView.resources,
                    packageName
                ) {
                    it.categoryName
                }
            } else {
                itemView.category_name.text = item.memo
            }
            sumOfMoney.text = abs(item.money).toString().localizeNumber(resources)
            txt_date.visibility = View.VISIBLE
            txt_date.text = dateWithPattern(item.date)

            setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }
        }


        private fun loadImage(categoryId: Int, categoryImage: String) {

            itemView.cardView.setCardBackgroundColor(
                itemView.resources.getColor(
                    CategoriesImageBackgroundColors.getCategoryColorById(categoryId)

                )
            )

            val categoryImageUrl = itemView.resources.getIdentifier(
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
                    itemView.resources
                )
            itemView.prg_percentage.progress = percentage.toInt()
            itemView.prg_percentage.max = calculatePercentage(biggestAmount, totalAmount).toInt()
        }

        private fun dateWithPattern(date: Int): String {
            return if (isCalendarSolar) {
                SolarCalendar.calcSolarCalendar(
                    date.times(1000L),

                    SolarCalendar.ShamsiPatterns.DETAIL_CHART_FRAGMENT, null, currentLocale
                )
            } else {
                val df = Date(date.times(1000L))
                SimpleDateFormat(DATE_PATTERN, currentLocale).format(df)
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
        fun onItemSelected(position: Int, item: Transaction)
    }


}

