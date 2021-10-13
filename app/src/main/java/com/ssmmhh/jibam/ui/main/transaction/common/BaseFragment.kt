package com.ssmmhh.jibam.ui.main.transaction.common

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ssmmhh.jibam.ui.UICommunicationListener
import com.ssmmhh.jibam.util.StateMessage
import com.ssmmhh.jibam.util.StateMessageCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
abstract class BaseFragment
constructor(
    @LayoutRes
    private val layoutRes: Int
) : Fragment(layoutRes) {
    private val TAG = "BaseTransactionFragment"

    lateinit var uiCommunicationListener: UICommunicationListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgressBar(false)
        handleLoading()
        handleStateMessages()
    }

    abstract fun handleStateMessages()

    abstract fun handleLoading()

    fun showProgressBar(isLoading: Boolean) {
        uiCommunicationListener.showProgressBar(isLoading)
    }

    fun handleNewStateMessage(
        stateMessage: StateMessage, removeMessageFromStack: () -> Unit
    ) {
        uiCommunicationListener.onResponseReceived(
            response = stateMessage.response,
            stateMessageCallback = object : StateMessageCallback {
                override fun removeMessageFromStack() {
                    removeMessageFromStack()
                }
            }
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement UICommunicationListener")
        }

    }
    fun getThemeAttributeColor(context: Context, resId: Int): Int {
        val value = TypedValue()
        context.getTheme().resolveAttribute(resId, value, true)
        return value.data
    }
    open fun navigateBack() {
        findNavController().navigateUp()
    }

}