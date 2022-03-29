package com.ssmmhh.jibam.util

import com.ssmmhh.jibam.util.Response
import com.ssmmhh.jibam.util.StateMessageCallback


interface UICommunicationListener {

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )

    fun hideSoftKeyboard()

    fun showProgressBar(isLoading: Boolean)

}