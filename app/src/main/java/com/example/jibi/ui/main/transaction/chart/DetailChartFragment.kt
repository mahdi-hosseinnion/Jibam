package com.example.jibi.ui.main.transaction.chart

import android.content.res.Resources
import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Transaction
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.ui.main.transaction.BaseTransactionFragment
import com.example.jibi.ui.main.transaction.MonthManger
import com.example.jibi.ui.main.transaction.home.TransactionFragmentDirections
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.util.mahdiLog
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
    private val monthManger: MonthManger,
    private val _resources: Resources
) : BaseTransactionFragment(
    R.layout.fragment_detail_chart,
    viewModelFactory,
    R.id.detailChartFragment_toolbar,
    _resources
), DetailChartListAdapter.Interaction {

    val args: DetailChartFragmentArgs by navArgs()

    override fun setTextToAllViews() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryId = args.categoryId
        if (categoryId > 0) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {

                monthManger.currentMonth.collect {

                    viewModel.launchNewJob(
                        TransactionStateEvent.OneShotOperationsTransactionStateEvent
                            .GetAllTransactionByCategoryId(
                            categoryId = categoryId,
                            fromDate = it.startOfMonth,
                            toDate = it.endOfMonth
                        )
                    )
                }
            }

            viewModel.launchNewJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.GetCategoryById(
                    categoryId
                )
            )
        } else {
            //TODO show unable snackBar and try again
        }
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.detailChartFields.let {
                    it.category?.let { category ->
                        setCategoryData(category)
                        it.allTransaction?.let { allTransactions ->
                            initRecyclerView(allTransactions, category)
                        }
                    }
                }
            }
        }
    }

    private fun setCategoryData(category: Category) {
        val monthName = " " + monthManger.getMonthName() + " " + _getString(R.string.month)

        detailChartFragment_toolbar.title = category.getCategoryNameFromStringFile(
            _resources,
            this.requireActivity().packageName
        ) { it.name } + monthName
    }

    private fun initRecyclerView(data: List<Record>, category: Category) {
        if (data.isNullOrEmpty()) {
            return
        }
        error_txt.visibility = View.GONE


        detail_chart_recycler.apply {
            layoutManager = LinearLayoutManager(this@DetailChartFragment.context)
            val recyclerAdapter = DetailChartListAdapter(
                interaction = this@DetailChartFragment,
                this@DetailChartFragment.requireActivity().packageName,
                requestManager,
                _resources,
                currentLocale,
                category,
                data
            )
            adapter = recyclerAdapter
        }
    }


    override fun onItemSelected(position: Int, item: Record) {
        viewModel.setDetailTransFields(item)
        navigateToAddTransactionFragment()
    }

    private fun navigateToAddTransactionFragment() {
        //on category selected and bottomSheet hided
        val action =
            DetailChartFragmentDirections.actionDetailChartFragmentToCreateTransactionFragment(
                isNewTransaction = false
            )
        findNavController().navigate(action)
    }

}