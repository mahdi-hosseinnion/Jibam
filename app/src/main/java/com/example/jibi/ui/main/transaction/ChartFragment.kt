package com.example.jibi.ui.main.transaction

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.ui.main.transaction.TransactionListAdapter.Companion.listOfColor
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.fragment_chart.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import kotlin.random.Random


//https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample/PieChartActivity.java

@ExperimentalCoroutinesApi
@FlowPreview
class ChartFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val _resources: Resources
) : BaseTransactionFragment(
    R.layout.fragment_chart,
    viewModelFactory,
    R.id.chartFragment_toolbar,
    _resources
), OnChartValueSelectedListener {
    override fun setTextToAllViews() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPieChart()
    }
    private fun initPieChart(){
        pie_chart.setUsePercentValues(true)
        pie_chart.getDescription().setEnabled(false)
        pie_chart.setExtraOffsets(5f, 10f, 5f, 5f)

        pie_chart.setDragDecelerationFrictionCoef(0.95f)

//        pie_chart.setCenterTextTypeface(tfLight)
//        pie_chart.setCenterText(generateCenterSpannableText())

        pie_chart.setDrawHoleEnabled(true)
        pie_chart.setHoleColor(Color.WHITE)

        pie_chart.setTransparentCircleColor(Color.WHITE)
        pie_chart.setTransparentCircleAlpha(110)

        pie_chart.setHoleRadius(58f)
        pie_chart.setTransparentCircleRadius(61f)

        pie_chart.setDrawCenterText(true)

        pie_chart.setRotationAngle(0f)
        // enable rotation of the chart by touch
        pie_chart.setRotationEnabled(true)
        pie_chart.setHighlightPerTapEnabled(true)

        // pie_chart.setUnit(" €");
        // pie_chart.setDrawUnitsInChart(true);

        // add a selection listener
        pie_chart.setOnChartValueSelectedListener(this)


        pie_chart.animateY(1400, Easing.EaseInOutQuad)
        // pie_chart.spin(2000, 0, 360);

        // pie_chart.spin(2000, 0, 360);
        val l: Legend = pie_chart.getLegend()
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f

        // entry label styling

        // entry label styling
        pie_chart.setEntryLabelColor(Color.BLACK)
//        pie_chart.setEntryLabelTypeface(tfRegular)
        pie_chart.setEntryLabelTextSize(12f)

        setDataToChart()
    }
    private fun setDataToChart(){
        val entries=ArrayList<PieEntry>()
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        val range=7
        for(i in 1..5){
            entries.add(PieEntry(i.times(10f), "number: $i"))
        }
        val dataSet=PieDataSet(entries, "")
        dataSet.setDrawIcons(false)

        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0F, 40F)
        dataSet.selectionShift = 5f
        //set color

        // add a lot of colors
        val colors = java.util.ArrayList<Int>()
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(resources.getColor(R.color.black))
//        data.setValueTypeface(tfLight)

        pie_chart.data = data
        // undo all highlights
        pie_chart.highlightValues(null)
        pie_chart.invalidate()
    }

    private fun generateCenterSpannableText(): SpannableString? {
        val s = SpannableString("MPAndroidChart\ndeveloped by Philipp Jahoda")
        s.setSpan(RelativeSizeSpan(1.7f), 0, 14, 0)
        s.setSpan(StyleSpan(Typeface.NORMAL), 14, s.length - 15, 0)
        s.setSpan(ForegroundColorSpan(Color.GRAY), 14, s.length - 15, 0)
        s.setSpan(RelativeSizeSpan(.8f), 14, s.length - 15, 0)
        s.setSpan(StyleSpan(Typeface.ITALIC), s.length - 14, s.length, 0)
        s.setSpan(ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length - 14, s.length, 0)
        return s
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {

    }

    override fun onNothingSelected() {
    }

}