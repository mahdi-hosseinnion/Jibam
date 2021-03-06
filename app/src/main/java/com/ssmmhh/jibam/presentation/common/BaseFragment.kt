package com.ssmmhh.jibam.presentation.common

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ssmmhh.jibam.util.ActivityCommunicationListener
import com.ssmmhh.jibam.data.util.StateMessage
import com.ssmmhh.jibam.data.util.StateMessageCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*

@FlowPreview
@ExperimentalCoroutinesApi
abstract class BaseFragment : Fragment() {
    private val TAG = "BaseTransactionFragment"

    lateinit var activityCommunicationListener: ActivityCommunicationListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgressBar(false)
        handleLoading()
        handleStateMessages()
    }

    abstract fun handleStateMessages()

    abstract fun handleLoading()

    fun showProgressBar(isLoading: Boolean) {
        activityCommunicationListener.showProgressBar(isLoading)
    }

    fun handleNewStateMessage(
        stateMessage: StateMessage, removeMessageFromStack: () -> Unit
    ) {
        activityCommunicationListener.onResponseReceived(
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
            activityCommunicationListener = context as ActivityCommunicationListener
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

    val locale: Locale
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                requireContext().resources.configuration.locales.get(0)
            } else {
                //noinspection deprecation
                requireContext().resources.configuration.locale;
            }
}