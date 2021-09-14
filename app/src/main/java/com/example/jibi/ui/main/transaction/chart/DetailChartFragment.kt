package com.example.jibi.ui.main.transaction.chart

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.Transaction
import com.example.jibi.models.TransactionEntity
import com.example.jibi.ui.main.transaction.MonthManger
import com.example.jibi.ui.main.transaction.chart.state.ChartStateEvent
import com.example.jibi.ui.main.transaction.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_detail_chart.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

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
) : BaseFragment(
    R.layout.fragment_detail_chart,
    R.id.detailChartFragment_toolbar,
    _resources
), DetailChartListAdapter.Interaction {

    val args: DetailChartFragmentArgs by navArgs()

    private val viewModel by viewModels<ChartViewModel> { viewModelFactory }


    override fun setTextToAllViews() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
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
                handleNewStateMessage(it){viewModel.clearStateMessage()}
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.getAllTransactionByCategoryId(
            args.categoryId
        ).observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                setCategoryData(
                    it[0].getCategoryNameFromStringFile(
                        _resources,
                        this.requireActivity().packageName
                    ) { it.categoryName }
                )
                initRecyclerView(it)
            }
        }
    }

    private fun setCategoryData(categoryName: String) {
        val monthName = " " + monthManger.getMonthName()

        detailChartFragment_toolbar.title = categoryName + monthName
    }

    private fun initRecyclerView(data: List<Transaction>) {
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
                data
            )
            adapter = recyclerAdapter
        }
    }


    override fun onItemSelected(position: Int, item: Transaction) {
        navigateToAddTransactionFragment(item.id)
    }

    private fun navigateToAddTransactionFragment(transactionId: Int) {
        //on category selected and bottomSheet hided
/*        val action =
            DetailChartFragmentDirections.actionDetailChartFragmentToCreateTransactionFragment(
                transactionId = transactionId
            )
        findNavController().navigate(action)*/
    }

}