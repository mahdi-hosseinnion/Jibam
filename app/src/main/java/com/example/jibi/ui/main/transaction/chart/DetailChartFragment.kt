package com.example.jibi.ui.main.transaction.chart

import android.content.res.Resources
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
import com.example.jibi.R
import com.example.jibi.models.Transaction
import com.example.jibi.repository.buildResponse
import com.example.jibi.ui.main.transaction.MonthManger
import com.example.jibi.ui.main.transaction.chart.ChartViewModel.Companion.FORCE_TO_NULL
import com.example.jibi.ui.main.transaction.common.BaseFragment
import com.example.jibi.util.*
import kotlinx.android.synthetic.main.fragment_detail_chart.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
                handleNewStateMessage(it) { viewModel.clearStateMessage() }
                if (it.response.message == _getString(R.string.transaction_successfully_deleted)) {
                    showDeleteUndoSnackBar()
                }
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
                        message = _getString(R.string.unable_to_restore_transaction),
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
                _getString(R.string.transaction_successfully_deleted),
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