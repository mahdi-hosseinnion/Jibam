package com.ssmmhh.jibam.feature_chart

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
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
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.FragmentChartBinding
import com.ssmmhh.jibam.data.model.ChartData
import com.ssmmhh.jibam.feature_chart.ChartFragment.ChartState.*
import com.ssmmhh.jibam.feature_common.BaseFragment
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity.Companion.EXPENSES_TYPE_MARKER
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity.Companion.INCOME_TYPE_MARKER
import com.ssmmhh.jibam.feature_chart.ChartFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import kotlin.collections.ArrayList


//https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample/PieChartActivity.java

@ExperimentalCoroutinesApi
@FlowPreview
class ChartFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
) : BaseFragment(), OnChartValueSelectedListener, ChartListAdapter.Interaction {

    private val TAG = "ChartFragment"

    private val viewModel by viewModels<ChartViewModel> { viewModelFactory }


    enum class ChartState {
        EXPENSES_STATE,
        INCOMES_STATE
    }

    private var currentChartState = EXPENSES_STATE

    private var chartData: List<ChartData> = ArrayList()

    private var _binding: FragmentChartBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPieChart()
        subscribeObservers()

        binding.toolbar.topAppBarMonth.setNavigationOnClickListener {
            navigateBack()
        }
        binding.toolbar.toolbarMonthChanger.toolbarMonth.setOnClickListener {
            viewModel.showMonthPickerBottomSheet(parentFragmentManager)
        }
        binding.toolbar.toolbarMonthChanger.monthManagerPrevious.setOnClickListener {
            viewModel.navigateToPreviousMonth()
        }
        binding.toolbar.toolbarMonthChanger.monthManagerNext.setOnClickListener {
            viewModel.navigateToNextMonth()
        }
        binding.fabSwap.setOnClickListener {
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
            binding.toolbar.topAppBarMonth.title = getString(R.string.income_chart_title)

            INCOME_TYPE_MARKER
        } else {
            binding.toolbar.topAppBarMonth.title = getString(R.string.expenses_chart_title)

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
        binding.pieChart.setUsePercentValues(false)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.setExtraOffsets(2f, 2f, 2f, 2f)

        binding.pieChart.setNoDataText(getString(R.string.no_chart_data_available))
        binding.pieChart.setNoDataTextColor(Color.RED)
        binding.pieChart.dragDecelerationFrictionCoef = 0.50f

//        pie_chart.setCenterTextTypeface(tfLight)
//        pie_chart.setCenterText(generateCenterSpannableText())

        binding.pieChart.isDrawHoleEnabled = true
        binding.pieChart.setHoleColor(
            getThemeAttributeColor(
                this.requireContext(),
                R.attr.colorSurface
            )
        )

        binding.pieChart.setTransparentCircleColor(
            getThemeAttributeColor(
                this.requireContext(),
                R.attr.colorSurface
            )
        )
        binding.pieChart.setTransparentCircleAlpha(50)

        binding.pieChart.holeRadius = 42f
        binding.pieChart.transparentCircleRadius = binding.pieChart.holeRadius.plus(3)

        binding.pieChart.setDrawCenterText(true)

        binding.pieChart.rotationAngle = 0f
        // enable rotation of the chart by touch
        binding.pieChart.isRotationEnabled = true
        binding.pieChart.isHighlightPerTapEnabled = true

        // pie_chart.setUnit(" €");
        // pie_chart.setDrawUnitsInChart(true);

        // add a selection listener
        binding.pieChart.setOnChartValueSelectedListener(this)


        binding.pieChart.animateY(1400, Easing.EaseInOutQuad)
//         binding.    pieChart.spin(2000, 0, 360);
        //legend: list next to chart
        val isLeftToRight =
            (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR)
        val l: Legend = binding.pieChart.legend
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
        binding.pieChart.setEntryLabelColor(
            getThemeAttributeColor(
                this.requireContext(),
                R.attr.colorOnSurface
            )
        )
//        pie_chart.setEntryLabelTypeface(tfRegular)
        binding.pieChart.setEntryLabelTextSize(12f)

    }

    private fun List<ChartData>.convertPieChartDataToPieEntry(): List<PieEntry> = this.map {
        PieEntry(
            it.percentage.toFloat() ?: 0f,
            it.getCategoryNameFromStringFile(requireContext())
        )
    }

    private fun setDataToChartAndRecyclerView(values: List<ChartData>) {
        val entries = ArrayList(values.convertPieChartDataToPieEntry())
        /**
         * we don't want ot show many entries of pie chart b/c the pie portion would be small
         * and messy
         */
        if (entries.size > CHART_MAX_COUNT_OF_DATA) {
            //get etc values from entries
            //we need to should make new arraylist otherwise we will get java.util.ConcurrentModificationException
            val etc: List<PieEntry> =
                ArrayList(
                    entries.subList(
                        CHART_MAX_COUNT_OF_DATA.minus(1),
                        entries.size
                    )
                )
            //remove etc values from main data
            entries.removeAll(etc)
            //add all of etc object values to single one and add it to entries
            val otherEntry = PieEntry(
                (etc.sumOf { it.value.toDouble() }).toFloat(),
                getString(R.string.etc)

            )
            entries.add(otherEntry)
        }
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
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        for (c in ColorTemplate.MATERIAL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(13f)
        data.setValueTextColor(resources.getColor(R.color.black))
//        data.setValueTypeface(tfLight)
        binding.pieChart.data = data
        try {
            if (values.isNullOrEmpty()) {
                binding.pieChart.clear()
            }
        } catch (e: Exception) {
            Log.e(TAG, "setDataToChartAndRecyclerView: ${e.message}", e)
        }
        // undo all highlights
        binding.pieChart.highlightValues(null)
        binding.pieChart.invalidate()
        initRecyclerView(values, colors)
    }

    private fun initRecyclerView(values: List<ChartData>, colors: List<Int>) {
        binding.chartRecycler.apply {
            layoutManager = LinearLayoutManager(this@ChartFragment.context)
            val recyclerAdapter = ChartListAdapter(
                this@ChartFragment,
                requestManager,
                currentLocale,
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
                viewState.currentMonth?.let {
                    binding.toolbar.toolbarMonthChanger.toolbarMonth.text = it.nameOfMonth
                }
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

    override fun onItemSelected(position: Int, item: ChartData) {
        val action =
            ChartFragmentDirections.actionChartFragmentToDetailChartFragment(
                categoryId = item.categoryId,
                categoryName = item.getCategoryNameFromStringFile(requireContext())
            )
        findNavController().navigate(action)
    }

    companion object {
        /** the max count of entry in pie chart */
        const val CHART_MAX_COUNT_OF_DATA = 8
    }
}