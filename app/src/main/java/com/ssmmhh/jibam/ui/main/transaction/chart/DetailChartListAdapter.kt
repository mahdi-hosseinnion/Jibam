package com.ssmmhh.jibam.ui.main.transaction.chart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.LayoutChartListItemBinding
import com.ssmmhh.jibam.persistence.dtos.TransactionDto
import com.ssmmhh.jibam.util.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class DetailChartListAdapter(
    private val interaction: Interaction? = null,
    private val isCalendarSolar: Boolean,
    private var requestManager: RequestManager,
    private var currentLocale: Locale,
    private var data: List<TransactionDto>? = null

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var totalAmount: Double = data?.sumOf { abs(it.money) } ?: 0.0
    private var biggestAmount: Double = data?.maxOf { abs(it.money) } ?: 0.0

    companion object {
        private const val DATE_PATTERN = "MM/dd/yyyy"

        private const val EMPTY_LIST_MARKER = -2
        private val EMPTY_LIST_MARKER_TRANSACTION = TransactionDto(
            id = EMPTY_LIST_MARKER,
            0.0,
            "",
            0,
            "",
            "",
            0
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
                binding = LayoutChartListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                interaction,
                isCalendarSolar,
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

    fun swapData(data: List<TransactionDto>?) {
        if (data.isNullOrEmpty()) {
            this.data = arrayListOf(EMPTY_LIST_MARKER_TRANSACTION)
        } else {
            this.data = data
            totalAmount = data.sumOf { abs(it.money) }
            biggestAmount = data.maxOf { abs(it.money) }
        }
        notifyDataSetChanged()
    }

    fun getTransaction(position: Int): TransactionDto? = this.data?.get(index = position)


    class DetailChartViewHolder(
        val binding: LayoutChartListItemBinding,
        private val interaction: Interaction?,
        private val isCalendarSolar: Boolean,
        private var requestManager: RequestManager,
        private var currentLocale: Locale
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: TransactionDto,
            totalAmount: Double,
            biggestAmount: Double
        ) = with(itemView) {
            loadImage(item)
            showPercentage(item.money, totalAmount, biggestAmount)
            //set text
            if (item.memo.isNullOrBlank()) {
                binding.categoryName.text = item.getCategoryNameFromStringFile(context)
            } else {
                binding.categoryName.text = item.memo
            }
            binding.sumOfMoney.text = abs(item.money).toString().localizeNumber(resources)
            binding.txtDate.visibility = View.VISIBLE
            binding.txtDate.text = dateWithPattern(item.date)

            setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }
        }


        private fun loadImage(item: TransactionDto) {

            binding.cardView.setCardBackgroundColor(
                itemView.resources.getColor(
                    CategoriesImageBackgroundColors.getCategoryColorById(item.categoryId)

                )
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
            money: Double,
            totalAmount: Double,
            biggestAmount: Double
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
        fun onItemSelected(position: Int, item: TransactionDto)
    }


}

