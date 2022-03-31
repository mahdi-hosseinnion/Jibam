package com.ssmmhh.jibam.presentation.chart

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
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.FragmentDetailChartBinding
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.data.source.repository.buildResponse
import com.ssmmhh.jibam.presentation.chart.ChartViewModel.Companion.FORCE_TO_NULL
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.math.BigDecimal
import java.util.*

@ExperimentalCoroutinesApi
@FlowPreview
class DetailChartFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences
) : BaseFragment(), DetailChartListAdapter.Interaction {

    val args: DetailChartFragmentArgs by navArgs()

    private val viewModel by viewModels<ChartViewModel> { viewModelFactory }

    private lateinit var recyclerAdapter: DetailChartListAdapter

    private var _binding: FragmentDetailChartBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailChartBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        initRecyclerView()
        subscribeObservers()
    }

    private fun setupUi() {
        binding.toolbar.topAppBarNormal.title =
            args.categoryName.replaceFirstChar { it.uppercase() }

        binding.toolbar.topAppBarNormal.setNavigationOnClickListener {
            navigateBack()
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
                if (it.response.message == getString(R.string.transaction_successfully_deleted)) {
                    showDeleteUndoSnackBar()
                }
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.getAllTransactionByCategoryId(
            args.categoryId
        ).observe(viewLifecycleOwner) {
            recyclerAdapter.swapData(it)
        }
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
                        val deletedTrans = adapter.getTransaction(viewHolder.adapterPosition)
                        swipeDeleteTransaction(deletedTrans)
                    }
                }

            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(this)

            adapter = recyclerAdapter
        }
    }

    private fun swipeDeleteTransaction(transactionToDelete: TransactionDto?) {
        if (transactionToDelete == null) {
            //show error to user
            return
        }
        //add to recently deleted
        viewModel.setRecentlyDeletedTrans(
            transactionToDelete
        )
        //delete from database
        viewModel.deleteTransaction(transactionToDelete.id)
        //show snackBar
    }

    private fun showDeleteUndoSnackBar() {
        val transaction = viewModel.getRecentlyDeletedTrans()

        val undoCallback = object : UndoCallback {
            override fun undo() {
                if (transaction == null) {
                    viewModel.addToMessageStack(
                        message = getString(R.string.unable_to_restore_transaction),
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    )
                    return
                }
                viewModel.insertRecentlyDeletedTrans(transaction)
            }

            override fun onDismiss() {
                //TODO remove this
                viewModel.setRecentlyDeletedTrans(
                    // an empty transaction with force to null memo to remove transaction in viewmodel
                    TransactionDto(
                        0,
                        BigDecimal.ZERO,
                        memo = FORCE_TO_NULL,
                        0,
                        "",
                        "",
                        "",
                        0
                    )
                )
            }
        }
        activityCommunicationListener.onResponseReceived(
            buildResponse(
                getString(R.string.transaction_successfully_deleted),
                UIComponentType.UndoSnackBar(undoCallback, binding.detailChartFragmentRoot),
                MessageType.Info
            ), object : StateMessageCallback {
                override fun removeMessageFromStack() {
                }
            }
        )
    }


    override fun onItemSelected(position: Int, item: TransactionDto) {
        navigateToAddTransactionFragment(item.id)
    }

    private fun navigateToAddTransactionFragment(transactionId: Int) {
        //on category selected and bottomSheet hided
        val action =
            DetailChartFragmentDirections.actionDetailChartFragmentToDetailEditTransactionFragment(
                transactionId = transactionId
            )
        findNavController().navigate(action)
    }

}