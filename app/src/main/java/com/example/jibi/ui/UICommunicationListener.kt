package com.example.jibi.ui

import com.example.jibi.util.Response
import com.example.jibi.util.StateMessageCallback


interface UICommunicationListener {

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )

    fun hideSoftKeyboard()

    fun showProgressBar(isLoading: Boolean)

    fun changeDrawerState(closeIt: Boolean)

    fun openDrawerMenu()

}