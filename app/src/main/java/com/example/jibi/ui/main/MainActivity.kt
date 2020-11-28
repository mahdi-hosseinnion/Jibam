package com.example.jibi.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.BaseApplication
import com.example.jibi.R
import com.example.jibi.models.Record
import com.example.jibi.persistence.RecordsDao
import com.example.jibi.ui.BaseActivity
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import kotlin.random.Random

@ExperimentalCoroutinesApi
class MainActivity : BaseActivity() {
    @Inject
    lateinit var providerFactory: ViewModelProvider.Factory

    val viewModel: MainViewModel by viewModels {
        providerFactory
    }
    val scope = CoroutineScope(IO)
    private val TAG = "MainActivity"

    @Inject
    lateinit var recordsDao: RecordsDao
    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var randomRecord = Record(0, Random.nextInt(100_000), "32", "324", Random.nextInt())
        viewModel.countOfActiveJobs.observe(this, Observer {
                printOnLog("trigger Count: $it")

            })
        viewModel.viewState.observe(this) { viewState ->
            viewState?.let {
                printOnLog("transactionList: ${it.transactionList}")
                printOnLog("summeryMoney: ${it.summeryMoney}")
            }
        }
        viewModel.stateMessage.observe(this){
            it?.let {
                printOnLog("stateMessage:$it")
            }
        }
        scope.launch {
            recordsDao.insertOrReplace(Record(0,-83,"ds","sdf",232223))
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
}