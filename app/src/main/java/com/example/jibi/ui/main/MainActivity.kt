package com.example.jibi.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jibi.BaseApplication
import com.example.jibi.R
import com.example.jibi.models.Record
import com.example.jibi.persistence.RecordsDao
import com.example.jibi.ui.BaseActivity
import com.example.jibi.ui.main.transaction.TransactionListAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MainActivity : BaseActivity(), TransactionListAdapter.Interaction {
    @Inject
    lateinit var providerFactory: ViewModelProvider.Factory

    val viewModel: MainViewModel by viewModels {
        providerFactory
    }
    private lateinit var recyclerAdapter: TransactionListAdapter
    val scope = CoroutineScope(IO)
    private val TAG = "MainActivity"

    @Inject
    lateinit var recordsDao: RecordsDao
    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRecyclerView()
        viewModel.countOfActiveJobs.observe(this, Observer { count ->
            showProgressBar(viewModel.areAnyJobsActive())
        })
        viewModel.viewState.observe(this) { viewState ->
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
        viewModel.stateMessage.observe(this) { stateMessage ->
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
                    this,
                    "Message: ${it.response.message} \n Type: ${it.response.uiComponentType} \n MessageType: ${it.response.messageType}",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.clearStateMessage()
            }
        }
    }

    private fun initRecyclerView() {

        transaction_recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerAdapter = TransactionListAdapter(
                null,
                this@MainActivity
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

    override fun inject() {
        (application as BaseApplication).mainComponent()
            .inject(this)
    }

    private fun <T> printList(
        data: List<T>,
        msg: String = ""

    ) {
        printOnLog("$msg +++++++++++++++++ size = ${data.size}")
        for (item in data) {
            printOnLog(msg + item.toString())
        }
    }

    private fun printOnLog(msg: String) = Log.d(TAG, "printOnLog: mahdi -> $msg")
    private fun now() = System.currentTimeMillis()
    override fun onItemSelected(position: Int, item: Record) {
    }

    override fun restoreListPosition() {
    }

    private fun showProgressBar(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
        }
    }
}