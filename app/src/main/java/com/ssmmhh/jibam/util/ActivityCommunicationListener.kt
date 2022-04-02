package com.ssmmhh.jibam.util

import com.ssmmhh.jibam.data.util.Response
import com.ssmmhh.jibam.data.util.StateMessageCallback


interface ActivityCommunicationListener {

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )

    fun hideSoftKeyboard()

    fun showProgressBar(isLoading: Boolean)

}