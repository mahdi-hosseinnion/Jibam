package com.example.jibi.ui

import androidx.appcompat.widget.Toolbar
import com.example.jibi.util.Response
import com.example.jibi.util.StateEvent
import com.example.jibi.util.StateMessageCallback


interface UICommunicationListener {

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )

//    fun displayProgressBar(isLoading: Boolean)

//    fun expandAppBar()

    fun hideSoftKeyboard()

    //    fun hideToolbar()
//
//    fun showToolbar()
    fun setupActionBarWithNavController(toolbar: Toolbar)

//    fun isStoragePermissionGranted(): Boolean
}