package com.example.jibi.ui.main.transaction.chart

import android.content.res.Resources
import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.ui.main.transaction.BaseTransactionFragment
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import kotlinx.android.synthetic.main.fragment_detail_chart.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

@ExperimentalCoroutinesApi
@FlowPreview
class DetailChartFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val _resources: Resources
) : BaseTransactionFragment(
    R.layout.fragment_detail_chart,
    viewModelFactory,
    R.id.detailChartFragment_toolbar,
    _resources
) {

    val args: DetailChartFragmentArgs by navArgs()

    override fun setTextToAllViews() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryId = args.categoryId
        if (categoryId > 0) {
            viewModel.launchNewJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.GetAllTransactionByCategoryId(
                    categoryId
                )
            )
            viewModel.launchNewJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.GetCategoryById(
                    categoryId
                )
            )
        } else {
            //TODO show unable snackBar and try again
        }
        subscribeObservers()
        setupChart()
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.detailChartFields.let {
                    it.category?.let { category ->
                        setCategoryDetail(category)
                    }
                    it.allTransaction?.let { allTransactions ->
                        setAllTransaction(allTransactions)
                    }
                }
            }
        }
    }

    private fun setCategoryDetail(category: Category) {
//        txt_test.text = txt_test.text.toString() + "\n" + category.toString()
    }

    private fun setAllTransaction(transactionList: List<Record>) {
//        txt_test.text = txt_test.text.toString() + "\n" + transactionList.toString()
        setDataToLineChart(transactionList)

    }

    fun setupChart() {
        // // Chart Style // //
        // background color
        lineChart.setBackgroundColor(Color.WHITE)

        // disable description text
        lineChart.getDescription().setEnabled(false)

        // enable touch gestures
        lineChart.setTouchEnabled(true)

        // set listeners
//        lineChart.setOnChartValueSelectedListener(this)
        lineChart.setDrawGridBackground(false)

        // create marker to display box when values are selected
//        val mv = MyMarkerView(this, R.layout.custom_marker_view)

        // Set the marker to the chart
//        mv.setChartView(lineChart)
//        lineChart.setMarker(mv)

        // enable scaling and dragging
        lineChart.setDragEnabled(true)
        lineChart.setScaleEnabled(true)
        // lineChart.setScaleXEnabled(true);

        // force pinch zoom along both axis
        lineChart.setPinchZoom(true)


        var xAxis: XAxis
        // // X-Axis Style // //
        xAxis = lineChart.getXAxis()

        // vertical grid lines
        xAxis.enableGridDashedLine(10f, 10f, 0f)


        var yAxis: YAxis
        // // Y-Axis Style // //
        yAxis = lineChart.getAxisLeft()

        // disable dual axis (only use LEFT axis)
        lineChart.getAxisRight().setEnabled(false)

        // horizontal grid lines
        yAxis.enableGridDashedLine(10f, 10f, 0f)

        // axis range
//        yAxis.axisMaximum = 200f
        yAxis.axisMinimum = 0f


        // // Create Limit Lines // //
        val llXAxis = LimitLine(9f, "Index 10")
        llXAxis.lineWidth = 4f
        llXAxis.enableDashedLine(10f, 10f, 0f)
        llXAxis.labelPosition = LimitLabelPosition.RIGHT_BOTTOM
        llXAxis.textSize = 10f
//        llXAxis.typeface = tfRegular
        val ll1 = LimitLine(150f, "Upper Limit")
        ll1.lineWidth = 4f
        ll1.enableDashedLine(10f, 10f, 0f)
        ll1.labelPosition = LimitLabelPosition.RIGHT_TOP
        ll1.textSize = 10f
//        ll1.typeface = tfRegular
        val ll2 = LimitLine(-30f, "Lower Limit")
        ll2.lineWidth = 4f
        ll2.enableDashedLine(10f, 10f, 0f)
        ll2.labelPosition = LimitLabelPosition.RIGHT_BOTTOM
        ll2.textSize = 10f
//        ll2.typeface = tfRegular

        // draw limit lines behind data instead of on top
        yAxis.setDrawLimitLinesBehindData(true)
        xAxis.setDrawLimitLinesBehindData(true)

        // add limit lines
        yAxis.addLimitLine(ll1)
        yAxis.addLimitLine(ll2)
        //xAxis.addLimitLine(llXAxis);


        // add data

        // draw points over time
        lineChart.animateX(1500)

        // get the legend (only possible after setting data)
        val l: Legend = lineChart.legend

        // draw legend entries as lines
        l.form = LegendForm.LINE
    }

    inline val randomNumber: Float get() = Random.nextInt(1, 10).toFloat()

    fun setDataToLineChart(data: List<Record>) {
        var values = ArrayList<Entry>()
        for (i in 1..30) {
            values.add(Entry(i.toFloat(), 0f))
        }
//
        for (item in data) {
            val cal = Calendar.getInstance()
            cal.time = Date(item.date.times(1000L))
            val day = cal[Calendar.DAY_OF_MONTH]
            val lastOne = values[day]
            values.add(day, Entry(day.toFloat(), abs(item.money).plus(lastOne.y).toFloat()))
        }
//        for (i in 0..1000) {
//            values.add(Entry(i.plus(i).toFloat(), randomNumber.times(i).plus(200f)))
//            val x=10
//            x.absoluteValue
//        }
        var set1: LineDataSet?

        if (lineChart.getData() != null &&
            lineChart.getData().getDataSetCount() > 0
        ) {

            set1 = lineChart.data.getDataSetByIndex(0) as (LineDataSet)
            set1.values = values
            set1.notifyDataSetChanged()
            lineChart.getData().notifyDataChanged()
            lineChart.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(values, "DataSet 1")
            set1.setDrawIcons(false)

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f)

            // black lines and points
            set1.color = Color.BLACK
            set1.setCircleColor(Color.BLACK)

            // line thickness and point size
            set1.lineWidth = 1f
            set1.circleRadius = 3f

            // draw points as solid circles
            set1.setDrawCircleHole(false)

            // customize legend entry
            set1.formLineWidth = 1f
            set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            set1.formSize = 15f

            // text size of values
            set1.valueTextSize = 9f

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f)

            // set the filled area
            set1.setDrawFilled(true)
            set1.fillFormatter =
                IFillFormatter { dataSet, dataProvider -> lineChart.getAxisLeft().getAxisMinimum() }

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                // drawables only supported on api level 18 and above
//                val drawable = ContextCompat.getDrawable(this, R.drawable.fade_red)
//                set1.fillDrawable = drawable
            } else {
                set1.fillColor = Color.BLACK
            }
            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)

            // set data
            lineChart.setData(data)
        }
    }


}