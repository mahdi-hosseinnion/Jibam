package com.ssmmhh.jibam.ui.main.transaction.chart

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.models.Transaction
import com.ssmmhh.jibam.repository.buildResponse
import com.ssmmhh.jibam.ui.main.transaction.chart.ChartViewModel.Companion.FORCE_TO_NULL
import com.ssmmhh.jibam.ui.main.transaction.common.BaseFragment
import com.ssmmhh.jibam.util.*
import kotlinx.android.synthetic.main.fragment_detail_chart.*
import kotlinx.android.synthetic.main.layout_toolbar_with_back_btn.*
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
) : BaseFragment(
    R.layout.fragment_detail_chart
), DetailChartListAdapter.Interaction {

    val args: DetailChartFragmentArgs by navArgs()

    private val viewModel by viewModels<ChartViewModel> { viewModelFactory }

    private lateinit var recyclerAdapter: DetailChartListAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        initRecyclerView()
        subscribeObservers()
    }

    private fun setupUi() {
        topAppBar_normal.title = args.categoryName.replaceFirstChar { it.uppercase() }

        topAppBar_normal.setNavigationOnClickListener {
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
        detail_chart_recycler.apply {

            layoutManager = LinearLayoutManager(this@DetailChartFragment.context)

            recyclerAdapter = DetailChartListAdapter(
                interaction = this@DetailChartFragment,
                this@DetailChartFragment.requireActivity().packageName,
                sharedPreferences.isCalendarSolar(currentLocale),
                requestManager,
                currentLocale
            )

            val swipeHandler =
                object : SwipeToDeleteCallback(this@DetailChartFragment.requireContext()) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val adapter = detail_chart_recycler.adapter as DetailChartListAdapter
                        val deletedTrans = adapter.getTransaction(viewHolder.adapterPosition)
                        swipeDeleteTransaction(deletedTrans)
                    }
                }

            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(this)

            adapter = recyclerAdapter
        }
    }

    private fun swipeDeleteTransaction(transactionToDelete: Transaction?) {
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
                viewModel.setRecentlyDeletedTrans(Transaction(0, 0.0, memo = FORCE_TO_NULL, 0.0))
            }
        }
        uiCommunicationListener.onResponseReceived(
            buildResponse(
                getString(R.string.transaction_successfully_deleted),
                UIComponentType.UndoSnackBar(undoCallback, detail_chart_fragment_root),
                MessageType.Info
            ), object : StateMessageCallback {
                override fun removeMessageFromStack() {
                }
            }
        )
    }


    override fun onItemSelected(position: Int, item: Transaction) {
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