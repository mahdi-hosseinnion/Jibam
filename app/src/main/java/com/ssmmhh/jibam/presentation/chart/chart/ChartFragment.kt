package com.ssmmhh.jibam.presentation.chart.chart

import android.graphics.Color
import android.os.Bundle
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
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.ChartData
import com.ssmmhh.jibam.databinding.FragmentChartBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.MonthChangerToolbarLayoutListener
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import com.ssmmhh.jibam.util.toLocaleString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*


//https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample/PieChartActivity.java

@ExperimentalCoroutinesApi
@FlowPreview
class ChartFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
) : BaseFragment(),
    ChartListAdapter.Interaction,
    ToolbarLayoutListener,
    MonthChangerToolbarLayoutListener {

    private val TAG = "ChartFragment"

    private val viewModel by viewModels<ChartViewModel> { viewModelFactory }

    private lateinit var binding: FragmentChartBinding

    private lateinit var recyclerAdapter: ChartListAdapter

    private val colors =
        ColorTemplate.VORDIPLOM_COLORS +
                ColorTemplate.LIBERTY_COLORS +
                ColorTemplate.MATERIAL_COLORS +
                ColorTemplate.COLORFUL_COLORS +
                ColorTemplate.JOYFUL_COLORS +
                ColorTemplate.PASTEL_COLORS

    private lateinit var pieDataSet: PieDataSet
    private lateinit var pieData: PieData


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChartBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            this.lifecycleOwner = this@ChartFragment.viewLifecycleOwner
            toolbarListener = this@ChartFragment
            monthChangerListener = this@ChartFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customizePieChart()
        setupChartData()
        initRecyclerView()
        subscribeObservers()
    }

    private fun customizePieChart() {
        binding.pieChart.apply {
            setUsePercentValues(false)
            description.isEnabled = false
            setExtraOffsets(2f, 2f, 2f, 2f)
            //No data text
            setNoDataText(getString(R.string.no_chart_data_available))
            setNoDataTextColor(Color.RED)
            dragDecelerationFrictionCoef = 0.50f
            isDrawHoleEnabled = true
            val surfaceColor = getThemeAttributeColor(
                this.context,
                R.attr.colorSurface
            )
            setHoleColor(surfaceColor)
            setTransparentCircleColor(surfaceColor)

            setTransparentCircleAlpha(50)
            holeRadius = 50f
            transparentCircleRadius = holeRadius.plus(3)

            setDrawCenterText(true)

            rotationAngle = 0f
            // enable rotation of the chart by touch
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1000, Easing.EasingOption.EaseInOutQuad)

            //legend: list next to chart
            legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.VERTICAL
            //Change chart's legend position to fit left to right languages(ex: Persian) too.
            val isLayoutDirectionLeftToRight =
                (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())
                        == ViewCompat.LAYOUT_DIRECTION_LTR)
            legend.horizontalAlignment = if (isLayoutDirectionLeftToRight)
                Legend.LegendHorizontalAlignment.RIGHT
            else
                Legend.LegendHorizontalAlignment.LEFT
            legend.setDrawInside(false)
            legend.xEntrySpace = 0f
            legend.yEntrySpace = 2f
            legend.form = Legend.LegendForm.CIRCLE
            legend.yOffset = 0f
            legend.textSize = 12f
            // entry label styling
            setEntryLabelColor(
                getThemeAttributeColor(
                    this.context,
                    R.attr.colorOnSurface
                )
            )
            setEntryLabelTextSize(12f)
        }
    }

    private fun setupChartData() {
        val chartLabel = if (viewModel.isChartTypeExpenses.value == true)
            getString(R.string.expenses)
        else
            getString(R.string.Income)

        pieDataSet = PieDataSet(null, chartLabel).apply {
            setDrawIcons(false)
            sliceSpace = 3f
            iconsOffset = MPPointF(0F, 40F)
            selectionShift = 5f
            colors = colors.toList()
        }

        pieData = PieData().apply {
            setValueFormatter(PercentFormatter())
            setValueTextSize(13f)
            setValueTextColor(resources.getColor(R.color.black))
        }

    }

    private fun initRecyclerView() {
        binding.chartRecycler.apply {
            layoutManager = LinearLayoutManager(this@ChartFragment.context)
            recyclerAdapter = ChartListAdapter(
                this@ChartFragment,
                requestManager,
                currentLocale,
                colors.toList()
            )
            isNestedScrollingEnabled = false
            adapter = recyclerAdapter
        }
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.currentMonth?.let {
                    val year = it.year?.let { "\n${it.toLocaleString()}" } ?: ""
                    binding.toolbarMonthName = resources.getString(it.monthNameResId) + year
                }
            }
        }
        viewModel.pieChartData.observe(viewLifecycleOwner) {
            it?.let { data ->
                updateChartData(data)
                recyclerAdapter.submitList(data)
            }
        }
    }

    private fun updateChartData(values: List<ChartData>) {
        if (values.isEmpty()) return
        pieDataSet.values = addEtcPieEntryIfThereAreTooManyEntries(
            values = convertPieChartDataToPieEntry(values)
        )
        pieData.dataSet = pieDataSet
        binding.pieChart.data = pieData
        // undo all highlights
        binding.pieChart.highlightValues(null)
        binding.pieChart.notifyDataSetChanged()
        binding.pieChart.invalidate()
    }

    /**
     * Prevent the chart from showing more than [CHART_MAX_COUNT_OF_DATA] entries in the pie chart
     * b/c the pie portion would be small and messy.
     */
    private fun addEtcPieEntryIfThereAreTooManyEntries(
        values: List<PieEntry>,
        maximumCountOfEntries: Int = CHART_MAX_COUNT_OF_DATA
    ): List<PieEntry> {
        val result = ArrayList(values)

        if (result.size > maximumCountOfEntries) {
            //get etc values from entries
            val etc: List<PieEntry> =
                ArrayList(
                    result.subList(
                        maximumCountOfEntries.minus(1),
                        result.size
                    )
                )
            //remove etc values from main data
            result.removeAll(etc)
            //add all of etc object values to single one and add it to entries
            val otherEntry = PieEntry(
                (etc.sumOf { it.value.toDouble() }).toFloat(),
                getString(R.string.etc)

            )
            result.add(otherEntry)
        }
        return result
    }

    private fun convertPieChartDataToPieEntry(value: List<ChartData>): List<PieEntry> = value.map {
        PieEntry(
            it.percentage,
            it.getCategoryNameFromStringFile(requireContext())
        )
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

    override fun onItemSelected(position: Int, item: ChartData) {
        val action =
            ChartFragmentDirections.actionChartFragmentToDetailChartFragment(
                categoryId = item.categoryId,
                categoryName = item.getCategoryNameFromStringFile(requireContext())
            )
        findNavController().navigate(action)
    }

    override fun onClickOnNavigation(view: View) {
        navigateBack()
    }

    override fun onClickOnMenuButton(view: View) {}

    override fun onClickOnMonthName(view: View) {
        viewModel.showMonthPickerBottomSheet(parentFragmentManager)
    }

    override fun onClickOnPreviousMonthButton(view: View) {
        viewModel.navigateToPreviousMonth()
    }

    override fun onClickOnNextMonthButton(view: View) {
        viewModel.navigateToNextMonth()
    }

    companion object {
        /** the max count of entry in pie chart */
        const val CHART_MAX_COUNT_OF_DATA = 8
    }

}