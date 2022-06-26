package com.ssmmhh.jibam.presentation.chart.detailchart

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssmmhh.jibam.databinding.FragmentDetailChartBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import com.ssmmhh.jibam.util.EventObserver
import com.ssmmhh.jibam.util.SwipeToDeleteCallback
import com.ssmmhh.jibam.util.isCalendarSolar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*

@ExperimentalCoroutinesApi
@FlowPreview
class DetailChartFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences
) : BaseFragment(), ToolbarLayoutListener {

    val args: DetailChartFragmentArgs by navArgs()

    private val viewModel by viewModels<DetailChartViewModel> { viewModelFactory }

    private lateinit var binding: FragmentDetailChartBinding

    private lateinit var recyclerAdapter: DetailChartListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailChartBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            toolbarListener = this@DetailChartFragment
            toolbarTitle = args.categoryName.replaceFirstChar { it.uppercase() }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        subscribeObservers()
        viewModel.start(categoryId = args.categoryId)
    }

    private fun initRecyclerView() {
        binding.detailChartRecycler.apply {

            layoutManager = LinearLayoutManager(this@DetailChartFragment.context)

            recyclerAdapter = DetailChartListAdapter(
                viewModel,
                sharedPreferences.isCalendarSolar(currentLocale),
            )

            val swipeHandler =
                object : SwipeToDeleteCallback(this@DetailChartFragment.requireContext()) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val adapter = binding.detailChartRecycler.adapter as DetailChartListAdapter
                        val deletedTrans =
                            adapter.getTransactionAt(viewHolder.adapterPosition) ?: return
                        viewModel.deleteTransaction(deletedTrans)
                    }
                }

            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(this)

            adapter = recyclerAdapter
        }
    }


    private fun subscribeObservers() {
        viewModel.transactions.observe(viewLifecycleOwner) {
            recyclerAdapter.submitData(it)
        }
        viewModel.navigateToTransactionDetail.observe(viewLifecycleOwner, EventObserver {
            navigateToAddTransactionFragment(it)
        })
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

    private fun navigateToAddTransactionFragment(transactionId: Int) {
        val action =
            DetailChartFragmentDirections.actionDetailChartFragmentToTransactionDetailFragment(
                transactionId = transactionId
            )
        findNavController().navigate(action)
    }

    override fun onClickOnNavigation(view: View) {
        navigateBack()
    }

    override fun onClickOnMenuButton(view: View) {}

}