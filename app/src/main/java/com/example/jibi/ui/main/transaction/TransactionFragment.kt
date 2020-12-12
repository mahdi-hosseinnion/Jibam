package com.example.jibi.ui.main.transaction

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Record
import com.example.jibi.ui.main.transaction.bottomSheet.CreateNewTransBottomSheet
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.util.mahdiLog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlin.random.Random

@ExperimentalCoroutinesApi
@MainScope
class TransactionFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory
) : BaseTransactionFragment(
    R.layout.fragment_transaction,
    viewModelFactory
), TransactionListAdapter.Interaction {
    private lateinit var recyclerAdapter: TransactionListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lastTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_transactionFragment_to_createTransactionFragment)
        }
        initRecyclerView()
        subscribeObservers()
        fab.setOnClickListener { view ->
            showBottomSheet()
        }
        txt_balance.setOnClickListener {
            insertRandomTransaction()
        }
    }


    private fun insertRandomTransaction() {
        viewModel.launchNewJob(
            TransactionStateEvent.OneShotOperationsTransactionStateEvent.InsertTransaction(
                Record(
                    id = 0,
                    money = Random.nextInt(-1000, 1000),
                    memo = "mme${(System.currentTimeMillis())}",
                    cat_id = Random.nextInt(42),
                    date = Random.nextInt()
                )
            )
        )
    }

    private fun showBottomSheet() {
        activity?.let {
            val modalBottomSheet =
                CreateNewTransBottomSheet(viewModel.viewState.value!!.categoryList!!)
            modalBottomSheet.show(it.supportFragmentManager, "CreateNewTransBottomSheet")
        }
    }

    private fun subscribeObservers() {
        viewModel.countOfActiveJobs.observe(viewLifecycleOwner, Observer { count ->

            showProgressBar(viewModel.areAnyJobsActive())
        })
        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
            viewState?.let {
                it.transactionList?.let { transactionList ->
                    recyclerAdapter.submitList(transactionList, true)
                }
                it.summeryMoney?.let { summeryMoney ->
                    summeryMoney.balance = (summeryMoney.income + summeryMoney.expenses)
                    txt_balance.text = "Balance: ${(summeryMoney.balance)}"
                    txt_expenses.text = "Expenses: ${summeryMoney.expenses}"
                    txt_income.text = "Income: ${summeryMoney.income}"
                }
            }
        }
        viewModel.stateMessage.observe(viewLifecycleOwner) { stateMessage ->
            stateMessage?.let {
                /*               if (isPaginationDone(stateMessage.response.message)) {
                viewModel.setQueryExhausted(true)
                viewModel.clearStateMessage()
            } else {
            uiCommunicationListener.onResponseReceived(
                response = it.response,
                stateMessageCallback = object : StateMessageCallback {
                    override fun removeMessageFromStack() {
                        viewModel.clearStateMessage()
                    }
                }
            )
        }*/
                Toast.makeText(
                    this@TransactionFragment.context,
                    "Message: ${it.response.message} \n Type: ${it.response.uiComponentType} \n MessageType: ${it.response.messageType}",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.clearStateMessage()
            }
        }
    }

    private fun initRecyclerView() {

        transaction_recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionFragment.context)
            recyclerAdapter = TransactionListAdapter(
                null,
                this@TransactionFragment
            )
//            addOnScrollListener(object: RecyclerView.OnScrollListener(){
//
//                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                    super.onScrollStateChanged(recyclerView, newState)
//                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                    val lastPosition = layoutManager.findLastVisibleItemPosition()
//                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
//                        Log.d(TAG, "BlogFragment: attempting to load next page...")
//                        viewModel.nextPage()
//                    }
//                }
//            })
            adapter = recyclerAdapter
        }

    }

    private fun showProgressBar(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.countOfNonCancellableJobs.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it>0){
                    viewModel.runPendingJobs()
                }
            }

        })

    }
    override fun onItemSelected(position: Int, item: Record) {
    }

    override fun restoreListPosition() {
    }
}