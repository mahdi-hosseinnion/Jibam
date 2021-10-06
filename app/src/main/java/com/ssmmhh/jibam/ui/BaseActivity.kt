package com.ssmmhh.jibam.ui

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ssmmhh.jibam.BaseApplication
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.util.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi

abstract class BaseActivity : AppCompatActivity(),
    UICommunicationListener {

    private val TAG = "BaseActivity"

    abstract fun inject()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var _resources: Resources

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApplication).appComponent
            .inject(this)
        super.onCreate(savedInstanceState)

    }

    private fun _getString(@StringRes resId: Int): String {
        return _resources.getString(resId)
    }

    override fun hideSoftKeyboard() {
        val imm: InputMethodManager =
            this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = this.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    override fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {

        when (response.uiComponentType) {

            is UIComponentType.AreYouSureDialog -> {

                response.message?.let {
                    areYouSureDialog(
                        message = it,
                        callback = response.uiComponentType.callback,
                        stateMessageCallback = stateMessageCallback
                    )
                }
            }

            is UIComponentType.DiscardOrSaveDialog -> {

                response.message?.let {
                    discardOrSaveDialog(
                        message = it,
                        callback = response.uiComponentType.callback,
                        stateMessageCallback = stateMessageCallback
                    )
                }
            }

            is UIComponentType.Toast -> {
                response.message?.let {
                    displayToast(
                        message = it,
                        stateMessageCallback = stateMessageCallback
                    )
                }
            }

            is UIComponentType.Dialog -> {
                displayDialog(
                    response = response,
                    stateMessageCallback = stateMessageCallback
                )
            }

            is UIComponentType.UndoSnackBar -> {
                displayUndoSnackBar(
                    message = response.message,
                    undoCallback = response.uiComponentType.callback,
                    parentView = response.uiComponentType.parentView,
                    stateMessageCallback = stateMessageCallback

                )
            }

            is UIComponentType.None -> {
                // This would be a good place to send to your Error Reporting
                // software of choice (ex: Firebase crash reporting)
                Log.i(TAG, "onResponseReceived: ${response.message}")
                stateMessageCallback.removeMessageFromStack()
            }
        }
    }

    private fun displayDialog(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {
        Log.d(TAG, "displayDialog: ")
        response.message?.let { message ->

            when (response.messageType) {

                is MessageType.Error -> {
                    displayErrorDialog(
                        message = message,
                        stateMessageCallback = stateMessageCallback
                    )
                }

                is MessageType.Success -> {
                    displaySuccessDialog(
                        message = message,
                        stateMessageCallback = stateMessageCallback
                    )
                }

                is MessageType.Info -> {
                    displayInfoDialog(
                        message = message,
                        stateMessageCallback = stateMessageCallback
                    )
                }

                else -> {
                    // do nothing
                    stateMessageCallback.removeMessageFromStack()
                    null
                }
            }
        } ?: stateMessageCallback.removeMessageFromStack()
    }

    private fun displaySuccessDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ) {
        AlertDialog.Builder(this)
            .setTitle(_getString(R.string.text_success))
            .setMessage(message)
            .setPositiveButton(_getString(R.string.text_ok)) { dialog, id ->
                stateMessageCallback.removeMessageFromStack()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    //    .setNegativeButton(R.string.cancel,
//    DialogInterface.OnClickListener
//    {
//        dialog, id ->
//        // User cancelled the dialog
//    })
    private fun displayErrorDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ) {
        AlertDialog.Builder(this)
            .setTitle(_getString(R.string.text_error))
            .setMessage(message)
            .setPositiveButton(_getString(R.string.text_ok)) { dialog, id ->
                stateMessageCallback.removeMessageFromStack()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun displayInfoDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ) {
        AlertDialog.Builder(this)
            .setTitle(_getString(R.string.text_info))
            .setMessage(message)
            .setPositiveButton(_getString(R.string.text_ok)) { dialog, id ->
                stateMessageCallback.removeMessageFromStack()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun areYouSureDialog(
        message: String,
        callback: AreYouSureCallback,
        stateMessageCallback: StateMessageCallback
    ) {
        AlertDialog.Builder(this)
            .setTitle(_getString(R.string.are_you_sure))
            .setMessage(message)
            .setPositiveButton(_getString(R.string.text_yes)) { dialog, id ->
                callback.proceed()
                stateMessageCallback.removeMessageFromStack()
            }
            .setNegativeButton(_getString(R.string.text_cancel)) { _, _ ->
                callback.cancel()
                stateMessageCallback.removeMessageFromStack()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun discardOrSaveDialog(
        message: String,
        callback: DiscardOrSaveCallback,
        stateMessageCallback: StateMessageCallback
    ) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(_getString(R.string.save)) { _, _ ->
                callback.save()
                stateMessageCallback.removeMessageFromStack()
            }
            .setNegativeButton(_getString(R.string.discard)) { _, _ ->
                callback.discard()
                stateMessageCallback.removeMessageFromStack()
            }
            .setCancelable(true)
            .setOnCancelListener {
                callback.cancel()
                stateMessageCallback.removeMessageFromStack()
            }
            .create()
            .show()
    }

    private fun displayUndoSnackBar(
        message: String?,
        undoCallback: UndoCallback,
        stateMessageCallback: StateMessageCallback,
        parentView: View
    ) {
        val snackbar: Snackbar = Snackbar.make(
            parentView, message ?: "No Message",
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction(_getString(R.string.snack_bar_undo)) { v ->
            undoCallback.undo()

        }
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                undoCallback.onDismiss()
            }

        })
        snackbar.show()
        stateMessageCallback.removeMessageFromStack()
    }

}