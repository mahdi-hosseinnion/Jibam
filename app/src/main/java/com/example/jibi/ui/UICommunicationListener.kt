package com.example.jibi.ui

import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.jibi.util.Response
import com.example.jibi.util.StateMessageCallback


interface UICommunicationListener {

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )

    fun hideSoftKeyboard()

    fun setupActionBarWithNavController(toolbar: Toolbar, drawerLayout: DrawerLayout? = null)

    fun showProgressBar(isLoading: Boolean)

    fun changeDrawerState(closeIt: Boolean)

}