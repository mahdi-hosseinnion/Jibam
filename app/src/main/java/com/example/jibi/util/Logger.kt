package com.example.jibi.util

import android.util.Log

var isUnitTest = false

fun mahdiLog(className: String?, message: String) {
    if (DEBUG && !isUnitTest) {
        Log.d(TAG, "$className: $message")
    } else if (DEBUG && isUnitTest) {
        println("$className: $message")
    }
}

//const
const val TAG = "AppDebug6724" // Tag for logs
const val DEBUG = true // enable logging
//throwExceptionIfDebugElseLogToCrashlytics