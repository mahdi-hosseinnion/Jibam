package com.example.jibi.ui.main.transaction.chart

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.models.PieChartData
import com.example.jibi.ui.main.transaction.chart.ChartFragment.ChartState.*
import com.example.jibi.ui.main.transaction.common.BaseFragment
import com.example.jibi.util.Constants.EXPENSES_TYPE_MARKER
import com.example.jibi.util.Constants.INCOME_TYPE_MARKER
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
import kotlinx.android.synthetic.main.toolbar_month_changer.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


//https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample/PieChartActivity.java

@ExperimentalCoroutinesApi
@FlowPreview
class ChartFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
) : BaseFragment(
    R.layout.fragment_chart,
    R.id.chartFragment_toolbar
), OnChartValueSelectedListener, ChartListAdapter.Interaction {

    private val TAG = "ChartFragment"

    private val viewModel by viewModels<ChartViewModel> { viewModelFactory }


    enum class ChartState {
        EXPENSES_STATE,
        INCOMES_STATE
    }

    private var currentChartState = EXPENSES_STATE

    private var chartData: List<PieChartData> = ArrayList()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPieChart()
        subscribeObservers()

        toolbar_month.setOnClickListener {
            viewModel.showMonthPickerBottomSheet(parentFragmentManager)
        }
        month_manager_previous.setOnClickListener {
            viewModel.navigateToPreviousMonth()
        }
        month_manager_next.setOnClickListener {
            viewModel.navigateToNextMonth()
        }
        fab_swap.setOnClickListener {
            swapChartCategory()
        }
    }

    private fun swapChartCategory() {
        //swap the chart information
        if (currentChartState == INCOMES_STATE) {
            currentChartState = EXPENSES_STATE
        } else {
            currentChartState = INCOMES_STATE
        }
        refreshChart()
    }

    private fun refreshChart() {
        val category_type_marker = if (currentChartState == INCOMES_STATE) {
            chartFragment_toolbar_title.text = getString(R.string.income_chart_title)
            INCOME_TYPE_MARKER
        } else {
            chartFragment_toolbar_title.text = getString(R.string.expenses_chart_title)

            EXPENSES_TYPE_MARKER
        }

        val filteredValues = chartData.filter { it.categoryType == category_type_marker }

//        pie_chart.data=null
//
//        if (filteredValues.isNullOrEmpty()) {
//            //TODO SHOW SNACKBAR TO TRY AGAIN
//        } else {
        setDataToChartAndRecyclerView(filteredValues)
//        }
    }

    private fun initPieChart() {
        pie_chart.setUsePercentValues(false)
        pie_chart.description.isEnabled = false
        pie_chart.setExtraOffsets(2f, 2f, 2f, 2f)

        pie_chart.setNoDataText(getString(R.string.no_chart_data_available))
        pie_chart.setNoDataTextColor(Color.RED)
        pie_chart.dragDecelerationFrictionCoef = 0.50f

//        pie_chart.setCenterTextTypeface(tfLight)
//        pie_chart.setCenterText(generateCenterSpannableText())

        pie_chart.isDrawHoleEnabled = true
        pie_chart.setHoleColor(Color.WHITE)

        pie_chart.setTransparentCircleColor(Color.WHITE)
        pie_chart.setTransparentCircleAlpha(110)

        pie_chart.holeRadius = 42f
        pie_chart.transparentCircleRadius = pie_chart.holeRadius.plus(3)

        pie_chart.setDrawCenterText(true)

        pie_chart.rotationAngle = 0f
        // enable rotation of the chart by touch
        pie_chart.isRotationEnabled = true
        pie_chart.isHighlightPerTapEnabled = true

        // pie_chart.setUnit(" €");
        // pie_chart.setDrawUnitsInChart(true);

        // add a selection listener
        pie_chart.setOnChartValueSelectedListener(this)


        pie_chart.animateY(1400, Easing.EaseInOutQuad)
//         pie_chart.spin(2000, 0, 360);
        //legend: list next to chart
        val isLeftToRight =
            (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR)
        val l: Legend = pie_chart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        if (isLeftToRight)
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        else
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT

        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.xEntrySpace = 0f
        l.yEntrySpace = 0f
        l.form = Legend.LegendForm.CIRCLE
        l.yOffset = 0f
        l.textSize = 12f


        // entry label styling
        pie_chart.setEntryLabelColor(Color.BLACK)
//        pie_chart.setEntryLabelTypeface(tfRegular)
        pie_chart.setEntryLabelTextSize(12f)

    }

    private fun List<PieChartData>.convertPieChartDataToPieEntry(): List<PieEntry> = this.map {
        PieEntry(
            it.percentage?.toFloat() ?: 0f,
            it.getCategoryNameFromStringFile(
                resources,
                this@ChartFragment.requireActivity().packageName
            ) { pi ->
                pi.categoryName
            }
        )
    }

    private fun setDataToChartAndRecyclerView(values: List<PieChartData>) {
        val entries = values.convertPieChartDataToPieEntry()
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        val chartLabel = if (currentChartState == INCOMES_STATE)
            getString(R.string.Income)
        else getString(R.string.expenses)

        val dataSet = PieDataSet(entries, chartLabel)
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
        try {
            if (values.isNullOrEmpty()) {
                pie_chart.clear()
            }
        } catch (e: Exception) {
            Log.e(TAG, "setDataToChartAndRecyclerView: ${e.message}", e)
        }
        // undo all highlights
        pie_chart.highlightValues(null)
        pie_chart.invalidate()
        initRecyclerView(values, colors)
    }

    private fun initRecyclerView(values: List<PieChartData>, colors: List<Int>) {
        chart_recycler.apply {
            layoutManager = LinearLayoutManager(this@ChartFragment.context)
            val recyclerAdapter = ChartListAdapter(
                this@ChartFragment,
                requestManager,

                currentLocale,
                this@ChartFragment.requireActivity().packageName,
                resources,
                colors
            )
            isNestedScrollingEnabled = false
            recyclerAdapter.submitList(values)
            adapter = recyclerAdapter
        }
    }

    override fun handleLoading() {
        viewModel.countOfActiveJobs.observe(
            viewLifecycleOwner
        ) {
            showProgressBar(viewModel.areAnyJobsActive())
        }
    }

    override fun handleStateMessages() {
        viewModel.stateMessage.observe(viewLifecycleOwner) {
            it?.let {
                handleNewStateMessage(it) { viewModel.clearStateMessage() }
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.currentMonth?.let { toolbar_month.text = it.nameOfMonth }
            }
        }
        viewModel.pieChartData.observe(viewLifecycleOwner) {
            it?.let { data ->
                chartData = data
                refreshChart()
            }
        }
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {

    }

    override fun onNothingSelected() {
    }

    override fun onItemSelected(position: Int, item: PieChartData) {
        val action =
            ChartFragmentDirections.actionChartFragmentToDetailChartFragment(item.categoryId)
        findNavController().navigate(action)
    }

}