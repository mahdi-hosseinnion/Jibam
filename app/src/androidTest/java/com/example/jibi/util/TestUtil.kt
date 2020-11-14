package com.example.jibi.util

import android.util.Log

abstract class TestUtil {
    val TEST_TAG = "mahdi debug"


    fun <T> printList(
        data: List<T>?,
        msg: String = ""

    ) {

        if (data == null) {
            printOnLog("$msg +++++++++++++++++ size = null")
            return
        }
        printOnLog("$msg +++++++++++++++++ size = ${data.size}")

        for (item in data) {
            printOnLog(msg + item.toString())
        }
    }

    fun printOnLog(msg: String) = Log.d(TEST_TAG, "printOnLog: mahdi -> $msg")
    fun now() = System.currentTimeMillis()
}