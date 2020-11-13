package com.example.jibi.ui.main

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.example.jibi.BaseApplication
import com.example.jibi.R
import com.example.jibi.models.Record
import com.example.jibi.persistence.RecordsDao
import com.example.jibi.ui.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : BaseActivity() {
    @Inject
    lateinit var recordsDao: RecordsDao
    val scope = CoroutineScope(IO)
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
/*
        recordsDao.loadAllRecordsBetweenDates(30, 80).observe(this, Observer {
            it?.let {
                printList(it, "RETURNED ROW")
            }
        })
        scope.launch {
            recordsDao.getAllRecords().collect {
                it?.let {
                    printList(it, "All")
                }
            }
        }
        scope.launch {
            delay(3000)
            recordsDao.insertOrReplace(Record(11,23,",","12",5))
        }
//        scope.launch {
//            for (i in 1..10) {
//                recordsDao.insertOrReplace(Record(i, 2323, "memo", "32", i*10))
//
//            }
//        }


*/

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
//            printOnLog(msg + item.toString())
        }
    }

    private fun printOnLog(msg: String) = Log.d(TAG, "printOnLog: mahdi -> $msg")
    private fun now() = System.currentTimeMillis()
}