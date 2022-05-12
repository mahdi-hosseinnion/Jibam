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
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.databinding.FragmentDetailChartBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import com.ssmmhh.jibam.util.SwipeToDeleteCallback
import com.ssmmhh.jibam.util.isCalendarSolar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*

@ExperimentalCoroutinesApi
@FlowPreview
class DetailChartFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences
) : BaseFragment(), DetailChartListAdapter.Interaction, ToolbarLayoutListener {

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
    }

    private fun initRecyclerView() {
        binding.detailChartRecycler.apply {

            layoutManager = LinearLayoutManager(this@DetailChartFragment.context)

            recyclerAdapter = DetailChartListAdapter(
                interaction = this@DetailChartFragment,
                sharedPreferences.isCalendarSolar(currentLocale),
                requestManager,
                currentLocale
            )

            val swipeHandler =
                object : SwipeToDeleteCallback(this@DetailChartFragment.requireContext()) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val adapter = binding.detailChartRecycler.adapter as DetailChartListAdapter
                        val deletedTrans = adapter.getTransactionAt(viewHolder.adapterPosition)
                        viewModel.deleteTransaction(deletedTrans)
                    }
                }

            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(this)

            adapter = recyclerAdapter
        }
    }


    private fun subscribeObservers() {
        viewModel.getAllTransactionByCategoryId(
            args.categoryId
        ).observe(viewLifecycleOwner) {
            recyclerAdapter.swapData(it)
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

    override fun onItemSelected(position: Int, item: TransactionDto) {
        navigateToAddTransactionFragment(item.id)
    }

    private fun navigateToAddTransactionFragment(transactionId: Int) {
        val action =
            DetailChartFragmentDirections.actionDetailChartFragmentToDetailEditTransactionFragment(
                transactionId = transactionId
            )
        findNavController().navigate(action)
    }

    override fun onClickOnNavigation(view: View) {
        navigateBack()
    }

    override fun onClickOnMenuButton(view: View) {}

}